package com.wiredi.runtime.messaging;

import com.wiredi.runtime.lang.ThrowingFunction;
import com.wiredi.runtime.messaging.errors.MessagingException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MessagingContextTest {

    @Test
    void testEmptyContext() {
        // Act
        MessagingContext context = MessagingContext.empty();

        // Assert
        assertThat(context.converters()).isEmpty();
        assertThat(context.messageInterceptors()).isEmpty();
    }

    @Test
    void testDefaultContext() {
        // Act
        MessagingContext context = MessagingContext.defaultContext();

        // Assert
        assertThat(context.converters()).isNotEmpty();
        assertThat(context.converters()).hasSize(2); // ByteArrayMessageConverter and StringMessageConverter
        assertThat(context.messageInterceptors()).isEmpty();
    }

    @Test
    void testConstructor() {
        // Arrange
        List<MessageConverter<?, ?>> converters = new ArrayList<>();
        converters.add(new TestStringConverter());

        List<MessageInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new TestInterceptor());

        // Act
        MessagingContext context = new MessagingContext(converters, interceptors);

        // Assert
        assertThat(context.converters()).hasSize(1);
        assertThat(context.messageInterceptors()).hasSize(1);
    }

    @Test
    void testCopy() {
        // Arrange
        MessagingContext original = MessagingContext.defaultContext();

        // Act
        MessagingContext copy = original.copy();

        // Assert
        assertThat(copy).isNotSameAs(original);
        assertThat(copy.converters()).hasSize(original.converters().size());
        assertThat(copy.messageInterceptors()).hasSize(original.messageInterceptors().size());
    }

    @Test
    void testConvertCacheAwareWithSuccessfulConversion() {
        // Arrange
        TestStringConverter converter = new TestStringConverter();
        MessagingContext context = new MessagingContext(List.of(converter), List.of());
        Message<MessageDetails> message = Message.just("test".getBytes(StandardCharsets.UTF_8));

        // Act
        String result = context.convertCacheAware(
                String.class,
                message.details(),
                c -> ((MessageConverter<String, MessageDetails>) c).deserialize(message, String.class)
        );

        // Assert
        assertThat(result).isEqualTo("converted: test");
        assertThat(converter.getDeserializeCalls()).isEqualTo(1);
    }

    @Test
    void testConvertCacheAwareWithCacheHit() {
        // Arrange
        TestStringConverter converter1 = new TestStringConverter();
        TestStringConverter converter2 = new TestStringConverter("converter2: ");

        MessagingContext context = new MessagingContext(List.of(converter1, converter2), List.of());
        Message<MessageDetails> message = Message.just("test".getBytes(StandardCharsets.UTF_8));

        // Act - First call should use converter1
        String result1 = context.convertCacheAware(
                String.class,
                message.details(),
                c -> ((MessageConverter<String, MessageDetails>) c).deserialize(message, String.class)
        );

        // Act - Second call should use the cached converter (converter1)
        String result2 = context.convertCacheAware(
                String.class,
                message.details(),
                c -> ((MessageConverter<String, MessageDetails>) c).deserialize(message, String.class)
        );

        // Assert
        assertThat(result1).isEqualTo("converted: test");
        assertThat(result2).isEqualTo("converted: test");
        assertThat(converter1.getDeserializeCalls()).isEqualTo(2);
        assertThat(converter2.getDeserializeCalls()).isEqualTo(0);
    }

    @Test
    void testConvertCacheAwareWithCacheMissAfterFailure() {
        // Arrange
        FailingAfterFirstCallConverter converter1 = new FailingAfterFirstCallConverter();
        TestStringConverter converter2 = new TestStringConverter("converter2: ");

        MessagingContext context = new MessagingContext(List.of(converter1, converter2), List.of());
        Message<MessageDetails> message = Message.just("test".getBytes(StandardCharsets.UTF_8));

        // Act - First call should use converter1
        String result1 = context.convertCacheAware(
                String.class,
                message.details(),
                c -> ((MessageConverter<String, MessageDetails>) c).deserialize(message, String.class)
        );

        // Act - Second call should try converter1 (from cache), fail, and then try converter2
        String result2 = context.convertCacheAware(
                String.class,
                message.details(),
                c -> ((MessageConverter<String, MessageDetails>) c).deserialize(message, String.class)
        );

        // Assert
        assertThat(result1).isEqualTo("first call");
        assertThat(result2).isEqualTo("converter2: test");
        assertThat(converter1.getDeserializeCalls()).isEqualTo(3); // Called 3 times: 1st success, 2nd fail, 3rd check if it can handle
        assertThat(converter2.getDeserializeCalls()).isEqualTo(1);
    }

    @Test
    void testConvertCacheAwareWithNoMatchingConverter() {
        // Arrange
        AlwaysFailingConverter converter = new AlwaysFailingConverter();
        MessagingContext context = new MessagingContext(List.of(converter), List.of());
        Message<MessageDetails> message = Message.just("test".getBytes(StandardCharsets.UTF_8));

        // Act & Assert
        assertThatThrownBy(() -> context.convertCacheAware(
                String.class,
                message.details(),
                c -> ((MessageConverter<String, MessageDetails>) c).deserialize(message, String.class)
        )).isInstanceOf(MessagingException.class)
          .hasMessageContaining("Unable to find converter");
    }

    @Test
    void testConvertCacheAwareWithConverterException() {
        // Arrange
        ThrowingConverter converter = new ThrowingConverter();
        MessagingContext context = new MessagingContext(List.of(converter), List.of());
        Message<MessageDetails> message = Message.just("test".getBytes(StandardCharsets.UTF_8));

        // Act & Assert
        assertThatThrownBy(() -> context.convertCacheAware(
                String.class,
                message.details(),
                c -> ((MessageConverter<String, MessageDetails>) c).deserialize(message, String.class)
        )).isInstanceOf(MessagingException.class)
          .hasMessageContaining("Unable to find converter");
    }

    // Test implementations

    private static class TestInterceptor implements MessageInterceptor {
        @Override
        public <D extends MessageDetails> Message<D> postConstruction(Message<D> message) {
            return message;
        }
    }

    private static class TestStringConverter implements MessageConverter<String, MessageDetails> {
        private final AtomicInteger deserializeCalls = new AtomicInteger(0);
        private final String prefix;

        public TestStringConverter() {
            this("converted: ");
        }

        public TestStringConverter(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public boolean canDeserialize(Message<?> message, Class<?> targetType) {
            return targetType == String.class;
        }

        @Override
        public String deserialize(Message<MessageDetails> message, Class<String> targetType) {
            deserializeCalls.incrementAndGet();
            return prefix + new String(message.body(), StandardCharsets.UTF_8);
        }

        @Override
        public boolean canSerialize(Object payload, MessageHeaders headers, MessageDetails messageDetails) {
            return payload instanceof String;
        }

        @Override
        public Message<MessageDetails> serialize(Object payload, MessageHeaders headers, MessageDetails messageDetails) {
            return Message.just(((String) payload).getBytes(StandardCharsets.UTF_8), headers, messageDetails);
        }

        public int getDeserializeCalls() {
            return deserializeCalls.get();
        }
    }

    private static class FailingAfterFirstCallConverter implements MessageConverter<String, MessageDetails> {
        private final AtomicInteger deserializeCalls = new AtomicInteger(0);

        @Override
        public boolean canDeserialize(Message<?> message, Class<?> targetType) {
            return targetType == String.class;
        }

        @Override
        public String deserialize(Message<MessageDetails> message, Class<String> targetType) {
            int calls = deserializeCalls.incrementAndGet();
            if (calls == 1) {
                return "first call";
            }
            return null;
        }

        @Override
        public boolean canSerialize(Object payload, MessageHeaders headers, MessageDetails messageDetails) {
            return false;
        }

        @Override
        public Message<MessageDetails> serialize(Object payload, MessageHeaders headers, MessageDetails messageDetails) {
            return null;
        }

        public int getDeserializeCalls() {
            return deserializeCalls.get();
        }
    }

    private static class AlwaysFailingConverter implements MessageConverter<String, MessageDetails> {
        private final AtomicInteger deserializeCalls = new AtomicInteger(0);

        @Override
        public boolean canDeserialize(Message<?> message, Class<?> targetType) {
            return targetType == String.class;
        }

        @Override
        public String deserialize(Message<MessageDetails> message, Class<String> targetType) {
            deserializeCalls.incrementAndGet();
            return null;
        }

        @Override
        public boolean canSerialize(Object payload, MessageHeaders headers, MessageDetails messageDetails) {
            return false;
        }

        @Override
        public Message<MessageDetails> serialize(Object payload, MessageHeaders headers, MessageDetails messageDetails) {
            return null;
        }
    }

    private static class ThrowingConverter implements MessageConverter<String, MessageDetails> {
        @Override
        public boolean canDeserialize(Message<?> message, Class<?> targetType) {
            return targetType == String.class;
        }

        @Override
        public String deserialize(Message<MessageDetails> message, Class<String> targetType) {
            throw new RuntimeException("Conversion error");
        }

        @Override
        public boolean canSerialize(Object payload, MessageHeaders headers, MessageDetails messageDetails) {
            return false;
        }

        @Override
        public Message<MessageDetails> serialize(Object payload, MessageHeaders headers, MessageDetails messageDetails) {
            return null;
        }
    }
}
