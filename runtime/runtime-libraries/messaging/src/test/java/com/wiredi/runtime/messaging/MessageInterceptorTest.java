package com.wiredi.runtime.messaging;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class MessageInterceptorTest {

    @Test
    void testPassThroughInterceptor() {
        // Arrange
        MessageInterceptor interceptor = new PassThroughInterceptor();
        Message<MessageDetails> originalMessage = Message.just("test".getBytes(StandardCharsets.UTF_8));

        // Act
        Message<MessageDetails> result = interceptor.postConstruction(originalMessage);

        // Assert
        assertThat(result).isSameAs(originalMessage);
    }

    @Test
    void testHeaderModifyingInterceptor() {
        // Arrange
        MessageInterceptor interceptor = new HeaderModifyingInterceptor();
        Message<MessageDetails> originalMessage = Message.just("test".getBytes(StandardCharsets.UTF_8));

        // Act
        Message<MessageDetails> result = interceptor.postConstruction(originalMessage);

        // Assert
        assertThat(result).isNotSameAs(originalMessage);
        assertThat(result.body()).isEqualTo(originalMessage.body());
        assertThat(result.header("intercepted")).isNotNull();
        assertThat(result.header("intercepted").decodeToString()).isEqualTo("true");
    }

    @Test
    void testContentModifyingInterceptor() {
        // Arrange
        MessageInterceptor interceptor = new ContentModifyingInterceptor();
        Message<MessageDetails> originalMessage = Message.just("test".getBytes(StandardCharsets.UTF_8));

        // Act
        Message<MessageDetails> result = interceptor.postConstruction(originalMessage);

        // Assert
        assertThat(result).isNotSameAs(originalMessage);
        assertThat(result.body()).isNotEqualTo(originalMessage.body());
        assertThat(new String(result.body(), StandardCharsets.UTF_8)).isEqualTo("INTERCEPTED: test");
    }

    @Test
    void testChainedInterceptors() {
        // Arrange
        MessageInterceptor interceptor1 = new HeaderModifyingInterceptor();
        MessageInterceptor interceptor2 = new ContentModifyingInterceptor();
        Message<MessageDetails> originalMessage = Message.just("test".getBytes(StandardCharsets.UTF_8));

        // Act
        Message<MessageDetails> result1 = interceptor1.postConstruction(originalMessage);
        Message<MessageDetails> result2 = interceptor2.postConstruction(result1);

        // Assert
        assertThat(result2).isNotSameAs(result1);
        assertThat(result2).isNotSameAs(originalMessage);
        assertThat(new String(result2.body(), StandardCharsets.UTF_8)).isEqualTo("INTERCEPTED: test");
        assertThat(result2.header("intercepted")).isNotNull();
        assertThat(result2.header("intercepted").decodeToString()).isEqualTo("true");
    }

    // Test implementations of MessageInterceptor

    private static class PassThroughInterceptor implements MessageInterceptor {
        @Override
        public <D extends MessageDetails> Message<D> postConstruction(Message<D> message) {
            return message;
        }
    }

    private static class HeaderModifyingInterceptor implements MessageInterceptor {
        @Override
        public <D extends MessageDetails> Message<D> postConstruction(Message<D> message) {
            return Message.builder(message.body())
                    .addHeaders(message.headers())
                    .addHeader("intercepted", "true")
                    .withDetails(message.details())
                    .build();
        }
    }

    private static class ContentModifyingInterceptor implements MessageInterceptor {
        @Override
        public <D extends MessageDetails> Message<D> postConstruction(Message<D> message) {
            String originalContent = new String(message.body(), StandardCharsets.UTF_8);
            String newContent = "INTERCEPTED: " + originalContent;
            return Message.builder(newContent.getBytes(StandardCharsets.UTF_8))
                    .addHeaders(message.headers())
                    .withDetails(message.details())
                    .build();
        }
    }
}