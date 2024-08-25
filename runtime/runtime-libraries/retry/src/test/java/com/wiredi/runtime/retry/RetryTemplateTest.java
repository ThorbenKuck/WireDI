package com.wiredi.runtime.retry;

import com.wiredi.runtime.lang.Counter;
import com.wiredi.runtime.lang.ThrowingRunnable;
import com.wiredi.runtime.retry.backoff.BackOffStrategy;
import com.wiredi.runtime.retry.exception.RetryFailedException;
import com.wiredi.runtime.retry.policy.RetryPolicy;
import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

class RetryTemplateTest {

    @Test
    public void verifyThatRetriedExceptionsAreRetriedCorrectly() {
        // Arrange
        TestCode testCode = new TestCode(RetryTestException::new);
        RetryTemplate retryTemplate = RetryTemplate.newInstance()
                .withRetryPolicy(
                        RetryPolicy.newInstance()
                                .configureExceptionBarrier(barrier -> barrier.retryIf()
                                        .throwableHasType(RetryTestException.class))
                                .withMaxRetries(3)
                                .build()
                )
                .build();

        // Act
        assertThatCode(() -> retryTemplate.execute(testCode)).isInstanceOf(RetryFailedException.class);

        // Assert
        assertThat(testCode.invocations.get()).isEqualTo(4);
    }

    @Test
    public void verifyThatANotConfiguredExceptionIsNotRetried() {
        // Arrange
        TestCode testCode = new TestCode(NullPointerException::new);
        RetryTemplate retryTemplate = RetryTemplate.newInstance()
                .withRetryPolicy(
                        RetryPolicy.newInstance()
                                .configureExceptionBarrier(barrier -> barrier.retryIf()
                                        .throwableHasType(RetryTestException.class))
                                .withMaxRetries(3)
                                .build()
                )
                .build();

        // Act
        assertThatCode(() -> retryTemplate.execute(testCode)).isInstanceOf(RetryFailedException.class);

        // Assert
        assertThat(testCode.invocations.get()).isEqualTo(1);
    }

    @RepeatedTest(20)
    public void verifyThatLinearBackOffStrategiesWaitForTheCorrectTime() {
        // Arrange
        TestCode testCode = new TestCode(RetryTestException::new);
        Duration expectedDuration = Duration.ofMillis(60);
        Duration precision = Duration.ofMillis(2);
        RetryTemplate retryTemplate = RetryTemplate.newInstance()
                .withRetryPolicy(
                        RetryPolicy.newInstance()
                                .configureExceptionBarrier(barrier -> barrier.retryIf()
                                        .throwableHasType(RetryTestException.class)
                                )
                                .withMaxRetries(3)
                                .build()
                )
                .withBackOff(BackOffStrategy.linear(Duration.of(10, ChronoUnit.MILLIS)))
                .build();

        // Act
        AbstractThrowableAssert<?, ? extends Throwable> exception = assertThatCode(
                () -> retryTemplate.execute(testCode)
        ).isInstanceOf(RetryFailedException.class);

        // Assert
        exception.satisfies(t -> {
            RetryFailedException retryFailedException = (RetryFailedException) t;
            assertThat(retryFailedException.getRetryState().totalDuration()).isCloseTo(expectedDuration, precision);
        });
        assertThat(testCode.invocations.get()).isEqualTo(4);
    }

    static class RetryTestException extends Exception {
    }

    private static class TestCode implements ThrowingRunnable<Throwable> {

        final Counter invocations = new Counter();
        final Supplier<Throwable> supplier;
        long lastInvocationTime = System.nanoTime();

        private TestCode(Supplier<Throwable> supplier) {
            this.supplier = supplier;
        }

        @Override
        public void run() throws Throwable {
            invocations.increment();
            lastInvocationTime = System.nanoTime();
            throw supplier.get();
        }
    }
}