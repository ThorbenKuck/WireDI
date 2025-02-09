package com.wiredi.runtime.messaging;

import com.wiredi.runtime.messaging.messages.SimpleMessage;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class MessageHeadersAccessorTest {

    @Test
    public void theMessageHeadersAccessorsIsAutoConfiguredInTheDefaultMessagingEngine() {
        // Arrange
        MessagingEngine messagingEngine = MessagingEngine.defaultEngine();
        MessageHeadersAccessor accessor = messagingEngine.requestContext().headersAccessor();
        SimpleMessage<MessageDetails> message = Message.builder("Test".getBytes())
                .addHeader(MessageHeader.of("test", true))
                .build();

        // Act
        AtomicReference<MessageHeaders> expectedHeaders = new AtomicReference<>(message.headers());
        AtomicReference<MessageHeaders> actualHeaders = new AtomicReference<>();
        messagingEngine.handleMessage(message, m -> {
            MessageHeaders currentHeaders = accessor.getCurrentHeaders();
            actualHeaders.set(currentHeaders);
        });

        // Assert
        assertThat(actualHeaders.get()).isEqualTo(expectedHeaders.get());
    }
}
