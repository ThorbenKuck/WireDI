package com.wiredi.runtime.retry;

import com.wiredi.runtime.retry.RetryTemplate;
import com.wiredi.runtime.time.Timed;
import com.wiredi.runtime.values.SafeReference;
import com.wiredi.runtime.retry.exception.RetryFailedException;
import com.wiredi.runtime.retry.policy.RetryPolicy;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class RetryTemplateTest {

    @Test
    public void test() {
        // Arrange
        RetryTemplate retryTemplate = RetryTemplate.newInstance()
                .withFixedBackOff(100, TimeUnit.MILLISECONDS)
                .withRetryPolicy(
                        RetryPolicy.newInstance()
                                .withMaxRetries(2)
                                .configureExceptionBarrier(barrier -> {
                                    barrier.retryIf().throwableHasType(NullPointerException.class);
                                })
                                .build()
                )
                .build();

        NullPointerException exception = new NullPointerException("Test");
        SafeReference<Throwable> raisedException = new SafeReference<>();

        // Act
        var timed = Timed.of(() -> {
            try {
                retryTemplate.execute(() -> {
                    throw exception;
                });
            } catch (Throwable e) {
                raisedException.set(e);
            }
        });

        // Assert
        assertThat(timed.get(TimeUnit.MILLISECONDS)).isGreaterThan(200);
        assertThat(raisedException.isPresent()).isTrue();
        assertThat(raisedException.get()).isInstanceOf(RetryFailedException.class);
        assertThat(raisedException.get()).isInstanceOf(RetryFailedException.class);
        RetryFailedException retryFailedException = (RetryFailedException) raisedException.get();
        assert retryFailedException != null;
        assertThat(retryFailedException.getErrors()).containsExactly(exception, exception, exception);
        assertThat(retryFailedException.getRetryState().attempt()).isEqualTo(3);
        assertThat(retryFailedException.getRetryState().timeout()).isEqualTo(Duration.ofMillis(100));
    }
}
