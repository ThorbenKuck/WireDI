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
        AtomicReference<MessageHeaders> headers = new AtomicReference<>();
        messagingEngine.handleMessage(message, m -> {
            MessageHeaders currentHeaders = accessor.getCurrentHeaders();
            headers.set(currentHeaders);
        });

        // Assert
        assertThat(headers.get()).isSameAs(message.headers());
    }

}