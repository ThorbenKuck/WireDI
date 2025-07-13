package com.wiredi.runtime.messaging;

import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.WiredApplication;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class MessageConverterAutoConfigurationTest {

    @Test
    public void testStringConversion() {
        // Arrange
        WireContainer wireRepository = WiredApplication.start().wireRepository();
        MessagingEngine messageConverters = wireRepository.get(MessagingEngine.class);

        // Act
        Message<MessageDetails> serialized = messageConverters.serialize("test");

        // Assert
        assertThat(serialized.body()).isEqualTo("test".getBytes());
    }

    @Test
    public void requestGeneralConsumer() {
        // Arrange
        WireContainer wireContainer = WiredApplication.start().wireRepository();

        // Act
        Consumer<Object> consumer = wireContainer.get(TypeIdentifier.of(Consumer.class).withGeneric(Object.class));

        // Assert
        consumer.accept("Hi");
    }
}