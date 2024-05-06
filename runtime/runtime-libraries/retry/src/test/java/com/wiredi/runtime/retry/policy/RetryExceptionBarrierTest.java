package com.wiredi.runtime.retry.policy;

import com.wiredi.runtime.retry.policy.RetryExceptionBarrier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class RetryExceptionBarrierTest {

    @Test
    @DisplayName("Verify that an empty RetryExceptionBarrier is passing any Exception")
    public void testEmptyAnyMatch() {
        // Arrange
        RetryExceptionBarrier retryExceptionBarrier = new RetryExceptionBarrier();

        // Act
        boolean passes = retryExceptionBarrier.passes(new NullPointerException());

        // Assert
        assertThat(passes).isTrue();
    }

    @Test
    @DisplayName("Verify that a RetryExceptionBarrier with only negative matches is passing any Exception")
    public void testEmptyPositiveMatch() {
        // Arrange
        RetryExceptionBarrier retryExceptionBarrier = new RetryExceptionBarrier()
                .doNotRetryIf().throwableHasType(IllegalStateException.class);

        // Act
        boolean passes = retryExceptionBarrier.passes(new NullPointerException());

        // Assert
        assertThat(passes).isTrue();
    }

    @Test
    @DisplayName("Verify that a RetryExceptionBarrier with any positive matches is not passing any Exception")
    public void testEmptyNegativeMatch() {
        // Arrange
        RetryExceptionBarrier retryExceptionBarrier = new RetryExceptionBarrier()
                .retryIf().throwableHasType(IllegalStateException.class);

        // Act
        boolean passes = retryExceptionBarrier.passes(new NullPointerException());

        // Assert
        assertThat(passes).isFalse();
    }

    @ParameterizedTest
    @DisplayName("Verify that throwable has type is working correctly for positive match")
    @MethodSource("allThrowable")
    public void positiveThrowableTest(Throwable throwable) {
        // Arrange
        RetryExceptionBarrier retryExceptionBarrier = new RetryExceptionBarrier()
                .retryIf().throwableHasType(throwable.getClass());

        // Act
        boolean passes = retryExceptionBarrier.passes(throwable);

        // Assert
        assertThat(passes).isTrue();
    }

    @ParameterizedTest
    @DisplayName("Verify that throwable has type is working correctly for negative match")
    @MethodSource("allThrowable")
    public void negativeThrowableTest(Throwable throwable) {
        // Arrange
        RetryExceptionBarrier retryExceptionBarrier = new RetryExceptionBarrier()
                .retryIf().throwableHasType(NullPointerException.class)
                .doNotRetryIf().throwableHasType(throwable.getClass());

        // Act
        boolean passes = retryExceptionBarrier.passes(throwable);

        // Assert
        assertThat(passes).isFalse();
    }

    @ParameterizedTest
    @DisplayName("Verify that throwable is instance of is working correctly for positive match of RuntimeExceptions")
    @MethodSource("allRuntimeExceptions")
    public void positiveInstanceTestForRuntimeException(Throwable throwable) {
        // Arrange
        RetryExceptionBarrier retryExceptionBarrier = new RetryExceptionBarrier()
                .retryIf().throwableIsInstanceOf(RuntimeException.class);

        // Act
        boolean passes = retryExceptionBarrier.passes(throwable);

        // Assert
        assertThat(passes).isTrue();
    }

    @ParameterizedTest
    @DisplayName("Verify that throwable is instance of is working correctly for negative match of RuntimeExceptions")
    @MethodSource("allRuntimeExceptions")
    public void negativeInstanceTestForRuntimeException(Throwable throwable) {
        // Arrange
        RetryExceptionBarrier retryExceptionBarrier = new RetryExceptionBarrier()
                .retryIf().throwableHasType(NullPointerException.class)
                .doNotRetryIf().throwableHasType(RuntimeException.class);

        // Act
        boolean passes = retryExceptionBarrier.passes(throwable);

        // Assert
        assertThat(passes).isFalse();
    }

    @ParameterizedTest
    @DisplayName("Verify that throwable is instance of is working correctly for positive match of Exception")
    @MethodSource("allExceptions")
    public void positiveInstanceTestForException(Throwable throwable) {
        // Arrange
        RetryExceptionBarrier retryExceptionBarrier = new RetryExceptionBarrier()
                .retryIf().throwableIsInstanceOf(Exception.class);

        // Act
        boolean passes = retryExceptionBarrier.passes(throwable);

        // Assert
        assertThat(passes).isTrue();
    }

    @ParameterizedTest
    @DisplayName("Verify that throwable is instance of is working correctly for negative match of Exception")
    @MethodSource("allExceptions")
    public void negativeInstanceTestForException(Throwable throwable) {
        // Arrange
        RetryExceptionBarrier retryExceptionBarrier = new RetryExceptionBarrier()
                .retryIf().throwableHasType(NullPointerException.class)
                .doNotRetryIf().throwableHasType(Exception.class);

        // Act
        boolean passes = retryExceptionBarrier.passes(throwable);

        // Assert
        assertThat(passes).isFalse();
    }

    @ParameterizedTest
    @DisplayName("Verify that throwable is instance of is working correctly for positive match of Exception")
    @MethodSource("allExceptions")
    public void positiveNoMatchInstanceTestForException(Throwable throwable) {
        // Arrange
        RetryExceptionBarrier retryExceptionBarrier = new RetryExceptionBarrier()
                .retryIf().throwableIsInstanceOf(RuntimeException.class);

        // Act
        boolean passes = retryExceptionBarrier.passes(throwable);

        // Assert
        assertThat(passes).isFalse();
    }

    static class RetryTestRuntimeException extends RuntimeException {}
    static class RetryTestException extends Exception {}
    static class NoRetryTestRuntimeException extends RuntimeException {}
    static class NoRetryTestException extends Exception {}

    private static List<Arguments> allThrowable() {
        return List.of(
                Arguments.of(new RetryTestRuntimeException()),
                Arguments.of(new RetryTestException()),
                Arguments.of(new NoRetryTestRuntimeException()),
                Arguments.of(new NoRetryTestException())
        );
    }

    private static List<Arguments> allExceptions() {
        return List.of(
                Arguments.of(new RetryTestException()),
                Arguments.of(new NoRetryTestException())
        );
    }

    private static List<Arguments> allRuntimeExceptions() {
        return List.of(
                Arguments.of(new RetryTestRuntimeException()),
                Arguments.of(new NoRetryTestRuntimeException())
        );
    }
}