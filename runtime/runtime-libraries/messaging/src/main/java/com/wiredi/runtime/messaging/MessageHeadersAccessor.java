package com.wiredi.runtime.messaging;

import com.wiredi.runtime.lang.ThrowingRunnable;
import com.wiredi.runtime.lang.ThrowingSupplier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * This class supports thread local access to {@link MessageHeaders}.
 * <p>
 * It can be used by integrations to transport headers across processes without the requirement to pass the headers
 * manually.
 * <p>
 * It is required that the integrations use this class.
 * If they don't, the header accessor is not able to determine headers.
 * Make sure to check the documentation of the integration to make sure this {@link MessageHeadersAccessor} is supported.
 */
public class MessageHeadersAccessor {

    private final ThreadLocal<MessageHeaders> currentThreadValues = new ThreadLocal<>();

    @Nullable
    public MessageHeaders getCurrentHeaders() {
        return currentThreadValues.get();
    }

    public <T extends Throwable> void doWith(MessageHeaders headers, ThrowingRunnable<T> runnable) throws T {
        MessageHeaders previous = currentThreadValues.get();
        try {
            currentThreadValues.set(headers);
            runnable.run();
        } finally {
            currentThreadValues.set(previous);
        }
    }

    public <V, T extends Throwable> V getWith(MessageHeaders headers, ThrowingSupplier<V, T> runnable) throws T {
        MessageHeaders previous = currentThreadValues.get();
        try {
            currentThreadValues.set(headers);
            return runnable.get();
        } finally {
            currentThreadValues.set(previous);
        }
    }

    public void ifPresent(Consumer<MessageHeaders> headersConsumer) {
        MessageHeaders current = currentThreadValues.get();
        headersConsumer.accept(current);
    }

    public void clear() {
        currentThreadValues.remove();
    }

    @Nullable
    public MessageHeaders set(@Nullable MessageHeaders headers) {
        MessageHeaders previousHeaders = this.currentThreadValues.get();
        this.currentThreadValues.set(headers);
        return previousHeaders;
    }
}
