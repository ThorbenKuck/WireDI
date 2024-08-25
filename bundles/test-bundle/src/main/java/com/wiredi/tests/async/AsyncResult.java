package com.wiredi.tests.async;

import com.wiredi.runtime.lang.ThrowingRunnable;
import org.junit.jupiter.api.Assertions;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.MultipleFailuresError;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AsyncResult<T> {

    private static final Duration DEFAULT_TIMEOUT = Duration.of(30, ChronoUnit.SECONDS);

    private int expectedInvocations = Integer.MIN_VALUE;
    private Semaphore barrier = new Semaphore(expectedInvocations);
    protected final List<AssertionFailedError> errors = new ArrayList<>();
    private final Supplier<T> initialValue;
    protected T value;

    protected AsyncResult() {
        this(() -> null);
    }

    protected AsyncResult(Supplier<T> initialValue) {
        this.initialValue = initialValue;
        this.value = initialValue.get();
    }

    public static <T> AsyncResultList<T> list() {
        return new AsyncResultList<>();
    }

    public static <T> AsyncResultValue<T> value() {
        return new AsyncResultValue<>();
    }

    public <E extends Throwable> void operate(ThrowingRunnable<E> operation) {
        try {
            operation.run();
        } catch (Throwable throwable) {
            this.errors.add(new AssertionFailedError("Unexpected error occurred", throwable));
        }
    }

    public void prime(int expectedInvocations) {
        this.expectedInvocations = expectedInvocations;
        barrier = new Semaphore(1 - expectedInvocations);
        value = initialValue.get();
        errors.clear();
    }

    public T get() {
        return get(DEFAULT_TIMEOUT);
    }

    public T get(Duration timeout) {
        checkErrors(false);
        traverse(timeout);
        checkErrors(true);
        T result = value;
        if (result == null) {
            Assertions.fail("AsyncResult was not filled");
        }
        return result;
    }

    public void await() {
        await(DEFAULT_TIMEOUT);
    }

    public void await(Duration timeout) {
        traverse(timeout);
    }

    protected void noteInvocation() {
        this.barrier.release();

        if (this.barrier.availablePermits() > 1) {
            this.errors.add(new AssertionFailedError("Too many invocations"));
        }
    }

    protected void noteInvocations(int invocations) {
        this.barrier.release(invocations);

        if (this.barrier.availablePermits() > 1) {
            this.errors.add(new AssertionFailedError("Too many invocations"));
        }
    }

    public boolean isCompleted() {
        return barrier.availablePermits() > 0;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "expectedInvocations=" + expectedInvocations +
                ", barrier=" + barrier +
                ", value=" + value +
                ", errors=" + errors +
                '}';
    }

    private void traverse(Duration timeout) {
        try {
            if (!barrier.tryAcquire(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                throw new AssertionFailedError("AsyncResultList was expected to be invoked " + expectedInvocations + " but was only invoked " + barrier.availablePermits());
            }
            barrier.release();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Traversing the barrier failed", e);
        }
    }

    private void checkErrors(boolean checkPermits) {
        if (errors.size() == 1) {
            throw errors.getFirst();
        } else if (errors.size() > 1) {
            throw new MultipleFailuresError(errors.size() + " invocations to many.", errors);
        }

        if (checkPermits) {
            int permits = barrier.availablePermits();
            if (permits < 1) {
                throw new AssertionFailedError("Async result was not invoked " + expectedInvocations + " times");
            }

            if (permits > 1) {
                throw new AssertionFailedError("Async result was invoked " + (permits - 1) + " too many times");
            }
        }
    }
}
