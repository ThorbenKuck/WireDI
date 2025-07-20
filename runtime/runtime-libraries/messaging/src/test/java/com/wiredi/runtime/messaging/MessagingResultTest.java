package com.wiredi.runtime.messaging;

import com.wiredi.runtime.messaging.errors.MessagingException;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MessagingResultTest {

    @Test
    void testSuccessResult() {
        // Arrange
        String resultValue = "test result";
        MessagingResult result = new MessagingResult.Success(resultValue);

        // Assert
        assertThat(result.wasSuccessful()).isTrue();
        assertThat(result.wasSkipped()).isFalse();
        assertThat(result.hasFailed()).isFalse();
        assertThat(result.error()).isNull();
        assertThat(result.result()).isEqualTo(resultValue);
        assertThat(result.<String>getResultAs()).isEqualTo(resultValue);
    }

    @Test
    void testSuccessResultEquality() {
        // Arrange
        MessagingResult result1 = new MessagingResult.Success("result1");
        MessagingResult result2 = new MessagingResult.Success("result2");
        MessagingResult result3 = new MessagingResult.Success(null);

        // Assert
        assertThat(result1).isEqualTo(result2); // Success instances are equal regardless of result value
        assertThat(result1).isEqualTo(result3);
        assertThat(result1).isNotEqualTo(null);
        assertThat(result1).isNotEqualTo("not a result");
    }

    @Test
    void testSkipMessageResult() {
        // Arrange
        MessagingResult result = new MessagingResult.SkipMessage();

        // Assert
        assertThat(result.wasSuccessful()).isFalse();
        assertThat(result.wasSkipped()).isTrue();
        assertThat(result.hasFailed()).isFalse();
        assertThat(result.error()).isNull();
        assertThat(result.result()).isNull();
    }

    @Test
    void testSkipMessageEquality() {
        // Arrange
        MessagingResult result1 = new MessagingResult.SkipMessage();
        MessagingResult result2 = new MessagingResult.SkipMessage();

        // Assert
        assertThat(result1).isEqualTo(result2);
        assertThat(result1).isNotEqualTo(null);
        assertThat(result1).isNotEqualTo("not a result");
    }

    @Test
    void testFailedResult() {
        // Arrange
        RuntimeException exception = new RuntimeException("Test exception");
        MessagingResult result = new MessagingResult.Failed(exception);

        // Assert
        assertThat(result.wasSuccessful()).isFalse();
        assertThat(result.wasSkipped()).isFalse();
        assertThat(result.hasFailed()).isTrue();
        assertThat(result.error()).isSameAs(exception);
        assertThat(result.result()).isNull();
    }

    @Test
    void testFailedResultEquality() {
        // Arrange
        RuntimeException exception1 = new RuntimeException("Test exception 1");
        RuntimeException exception2 = new RuntimeException("Test exception 2");
        MessagingResult result1 = new MessagingResult.Failed(exception1);
        MessagingResult result2 = new MessagingResult.Failed(exception1);
        MessagingResult result3 = new MessagingResult.Failed(exception2);

        // Assert
        assertThat(result1).isEqualTo(result2);
        assertThat(result1).isNotEqualTo(result3);
        assertThat(result1).isNotEqualTo(null);
        assertThat(result1).isNotEqualTo("not a result");
    }

    @Test
    void testErrorOrMethod() {
        // Arrange
        RuntimeException exception = new RuntimeException("Test exception");
        RuntimeException fallbackException = new RuntimeException("Fallback exception");
        MessagingResult successResult = new MessagingResult.Success("test");
        MessagingResult failedResult = new MessagingResult.Failed(exception);

        // Act & Assert
        assertThat(successResult.errorOr(() -> fallbackException)).isSameAs(fallbackException);
        assertThat(failedResult.errorOr(() -> fallbackException)).isSameAs(exception);
    }

    @Test
    void testTryPropagateErrorWithRuntimeException() {
        // Arrange
        RuntimeException exception = new RuntimeException("Test exception");
        MessagingResult result = new MessagingResult.Failed(exception);

        // Act & Assert
        assertThatThrownBy(() -> result.tryPropagateError())
                .isSameAs(exception);
    }

    @Test
    void testTryPropagateErrorWithCheckedException() {
        // Arrange
        Exception exception = new Exception("Test exception");
        MessagingResult result = new MessagingResult.Failed(exception);

        // Act & Assert
        assertThatThrownBy(() -> result.tryPropagateError())
                .isInstanceOf(MessagingException.class)
                .hasCause(exception);
    }

    @Test
    void testTryPropagateErrorWithConverter() {
        // Arrange
        RuntimeException originalException = new RuntimeException("Original exception");
        RuntimeException convertedException = new RuntimeException("Converted exception");
        Function<Throwable, Throwable> converter = e -> convertedException;
        MessagingResult result = new MessagingResult.Failed(originalException);

        // Act & Assert
        assertThatThrownBy(() -> result.tryPropagateError(converter))
                .isSameAs(convertedException);
    }

    @Test
    void testCustomMessagingResult() {
        // Arrange
        MessagingResult customResult = new CustomMessagingResult();

        // Assert
        assertThat(customResult.wasSuccessful()).isFalse(); // Default implementation
        assertThat(customResult.wasSkipped()).isFalse(); // Default implementation
        assertThat(customResult.hasFailed()).isFalse(); // Default implementation
        assertThat(customResult.error()).isNull(); // Default implementation
        assertThat(customResult.result()).isNull(); // Default implementation
    }

    private static class CustomMessagingResult implements MessagingResult {
        // Empty implementation using all default methods
    }
}