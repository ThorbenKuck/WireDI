package com.wiredi.runtime.messaging;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class MessageFilterTest {

    @Test
    void testAlwaysSkipFilter() {
        // Arrange
        MessageFilter filter = message -> true;
        Message<MessageDetails> message = Message.just("test".getBytes(StandardCharsets.UTF_8));

        // Act
        boolean result = filter.shouldSkip(message);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void testNeverSkipFilter() {
        // Arrange
        MessageFilter filter = message -> false;
        Message<MessageDetails> message = Message.just("test".getBytes(StandardCharsets.UTF_8));

        // Act
        boolean result = filter.shouldSkip(message);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void testSkipBasedOnHeader() {
        // Arrange
        MessageFilter filter = message -> {
            MessageHeader header = message.header("skip");
            return header != null && "true".equals(header.decodeToString());
        };

        Message<MessageDetails> messageToSkip = Message.builder("test".getBytes(StandardCharsets.UTF_8))
                .addHeader("skip", "true")
                .build();

        Message<MessageDetails> messageToProcess = Message.builder("test".getBytes(StandardCharsets.UTF_8))
                .addHeader("skip", "false")
                .build();

        Message<MessageDetails> messageWithoutHeader = Message.just("test".getBytes(StandardCharsets.UTF_8));

        // Act & Assert
        assertThat(filter.shouldSkip(messageToSkip)).isTrue();
        assertThat(filter.shouldSkip(messageToProcess)).isFalse();
        assertThat(filter.shouldSkip(messageWithoutHeader)).isFalse();
    }

    @Test
    void testSkipBasedOnBody() {
        // Arrange
        MessageFilter filter = message -> {
            String body = new String(message.body(), StandardCharsets.UTF_8);
            return body.contains("skip");
        };

        Message<MessageDetails> messageToSkip = Message.just("please skip this".getBytes(StandardCharsets.UTF_8));
        Message<MessageDetails> messageToProcess = Message.just("process this".getBytes(StandardCharsets.UTF_8));

        // Act & Assert
        assertThat(filter.shouldSkip(messageToSkip)).isTrue();
        assertThat(filter.shouldSkip(messageToProcess)).isFalse();
    }
}