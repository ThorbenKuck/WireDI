package com.wiredi.lang.values;

import com.wiredi.lang.Preconditions;
import com.wiredi.lang.async.AsyncLoader;
import com.wiredi.lang.async.Barrier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.wiredi.lang.Preconditions.notNull;

public class FutureValue<T> implements Value<T> {

    private final Barrier barrier = Barrier.closed();
    private T content;
    private Throwable throwable;

    public FutureValue(CompletionStage<@NotNull T> future) {
        future.whenComplete((t, throwable) -> {
            this.content = t;
            this.throwable = throwable;
            this.barrier.open();
        });
    }

    public static <T> FutureValue<@NotNull T> of(@NotNull T t) {
        return new FutureValue<>(CompletableFuture.completedFuture(t));
    }

    public static <T> FutureValue<@NotNull T> of(Supplier<@NotNull T> supplier) {
        return new FutureValue<>(AsyncLoader.load(supplier));
    }

    public boolean isAvailable() {
        return barrier.isOpen();
    }

    @Nullable
    private T getValue() throws InterruptedException {
        barrier.traverse();
        T current = content;
        Throwable exception = throwable;

        if (exception != null) {
            throw new IllegalStateException(exception);
        }

        return current;
    }

    @NotNull
    public T get() {
        try {
            T current = getValue();
            return notNull(current, () -> "A FutureValue should never contain null");
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void set(@NotNull T t) {
        this.content = t;
        barrier.open();
    }

    @Override
    public boolean isSet() {
        return isAvailable();
    }

    @Override
    public void ifEmpty(@NotNull Runnable runnable) {
        T current = content;

        if (current == null) {
            runnable.run();
        }
    }

    @Override
    public @NotNull IfPresentStage ifPresent(@NotNull Consumer<T> presentConsumer) {
        T current = content;

        if (current != null) {
            presentConsumer.accept(current);
            return IfPresentStage.wasPresent();
        } else {
            return IfPresentStage.wasMissing();
        }
    }

    @NotNull
    public T await() throws ExecutionException {
        barrier.traverse();
        T current = content;
        Throwable exception = throwable;

        if (exception != null) {
            throw new ExecutionException(exception);
        }
        return notNull(current, () -> "A FutureValue should never contain null");
    }
}
