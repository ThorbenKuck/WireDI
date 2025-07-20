package com.wiredi.runtime.messaging;

import com.wiredi.runtime.messaging.errors.MessagingException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MessagingErrorHandlerTest {

    @Test
    void testDefaultErrorHandler() {
        // Arrange
        MessagingErrorHandler handler = MessagingErrorHandler.DEFAULT;
        Message<MessageDetails> message = Message.just("test".getBytes(StandardCharsets.UTF_8));
        RuntimeException exception = new RuntimeException("Test exception");

        // Act
        MessagingResult result = handler.handleError(message, exception);

        // Assert
        assertThat(result).isInstanceOf(MessagingResult.Failed.class);
        MessagingResult.Failed failedResult = (MessagingResult.Failed) result;
        assertThat(failedResult.error()).isSameAs(exception);
    }

    @Test
    void testRethrowErrorHandlerWithRuntimeException() {
        // Arrange
        MessagingErrorHandler handler = MessagingErrorHandler.RETHROW;
        Message<MessageDetails> message = Message.just("test".getBytes(StandardCharsets.UTF_8));
        RuntimeException exception = new RuntimeException("Test exception");

        // Act & Assert
        assertThatThrownBy(() -> handler.handleError(message, exception))
                .isSameAs(exception);
    }

    @Test
    void testRethrowErrorHandlerWithCheckedException() {
        // Arrange
        MessagingErrorHandler handler = MessagingErrorHandler.RETHROW;
        Message<MessageDetails> message = Message.just("test".getBytes(StandardCharsets.UTF_8));
        Exception exception = new Exception("Test exception");

        // Act & Assert
        assertThatThrownBy(() -> handler.handleError(message, exception))
                .isInstanceOf(MessagingException.class)
                .hasCause(exception);
    }

    @Test
    void testCustomErrorHandler() {
        // Arrange
        MessagingErrorHandler handler = (message, throwable) -> {
            if (throwable.getMessage().contains("skip")) {
                return new MessagingResult.SkipMessage();
            } else {
                return new MessagingResult.Failed(throwable);
            }
        };

        Message<MessageDetails> message = Message.just("test".getBytes(StandardCharsets.UTF_8));
        RuntimeException skipException = new RuntimeException("Please skip this message");
        RuntimeException failException = new RuntimeException("This is a failure");

        // Act
        MessagingResult skipResult = handler.handleError(message, skipException);
        MessagingResult failResult = handler.handleError(message, failException);

        // Assert
        assertThat(skipResult).isInstanceOf(MessagingResult.SkipMessage.class);
        assertThat(failResult).isInstanceOf(MessagingResult.Failed.class);
        MessagingResult.Failed failedResult = (MessagingResult.Failed) failResult;
        assertThat(failedResult.error()).isSameAs(failException);
    }
}