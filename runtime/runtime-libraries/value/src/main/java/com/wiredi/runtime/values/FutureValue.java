package com.wiredi.runtime.values;

import com.wiredi.runtime.async.AsyncLoader;
import com.wiredi.runtime.async.barriers.SemaphoreBarrier;
import com.wiredi.runtime.lang.ThrowingSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.wiredi.runtime.lang.Preconditions.isNotNull;
import static com.wiredi.runtime.lang.Preconditions.is;

/**
 * A Value that listens to a {@link CompletionStage},
 * updating its state when the linked {@link CompletionStage} is completed.
 *
 * @param <T>
 */
public class FutureValue<T> implements Value<T> {

    private final SemaphoreBarrier barrier = SemaphoreBarrier.closed();
    @Nullable
    private CompletionStageSubscription<T> subscription;
    private T content;
    private Throwable throwable;

    public FutureValue(CompletionStage<@NotNull T> completionStage) {
        this.subscription = newSubscription(completionStage);
    }

    public static <T> FutureValue<@NotNull T> of(@Nullable T t) {
        return new FutureValue<>(CompletableFuture.completedFuture(t));
    }

    public static <T> FutureValue<@NotNull T> of(ThrowingSupplier<@NotNull T, ?> supplier) {
        return new FutureValue<>(AsyncLoader.load(supplier));
    }

    @NotNull
    private CompletionStageSubscription<T> newSubscription(@NotNull CompletionStage<@NotNull T> completionStage) {
        return CompletionStageSubscription.createFor(completionStage)
                .onCompletion(this::unsafeSet)
                .onCompletion(() -> this.subscription = null)
                .build();
    }

    public boolean isAvailable() {
        return barrier.isOpen();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    private T getValue() throws InterruptedException {
        barrier.traverse();
        T current = content;
        tryThrowException();

        return current;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    public T get() {
        try {
            T current = getValue();
            return isNotNull(current, () -> "A FutureValue should never contain null");
        } catch (final InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    public T get(Duration duration) throws InterruptedException {
        barrier.traverse(duration);
        T current = content;
        tryThrowException();

        return current;
    }

    public void unsafeSet(
            @Nullable T content,
            @Nullable Throwable throwable
    ) {
        is(content != null || throwable != null, () -> "Either the content, or the exception need top be provided");
        this.content = content;
        this.throwable = throwable;
        this.barrier.open();
    }

    public void cancelCurrentSubscription() {
        CompletionStageSubscription<T> currentSubscription = this.subscription;
        if (currentSubscription != null) {
            currentSubscription.cancel();
            this.subscription = null;
        }
    }

    public void set(@NotNull CompletionStage<@NotNull T> completionStage) {
        cancelCurrentSubscription();
        this.barrier.close();
        this.content = null;
        this.throwable = null;
        this.subscription = newSubscription(completionStage);
    }

    public void setAndCancel(@Nullable T t) {
        cancelCurrentSubscription();
        set(t);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(@Nullable T t) {
        set(CompletableFuture.completedStage(t));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSet() {
        return isAvailable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void ifEmpty(@NotNull Runnable runnable) {
        if (content == null) {
            runnable.run();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull T getOrSet(Supplier<@NotNull T> supplier) {
        if (content != null) {
            return content;
        }

        T newContent = supplier.get();
        set(newContent);
        return newContent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull IfPresentStage ifPresent(@NotNull Consumer<T> presentConsumer) {
        T current = content;
        tryThrowException();

        if (current != null) {
            presentConsumer.accept(current);
            return IfPresentStage.wasPresent();
        } else {
            return IfPresentStage.wasMissing();
        }
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    public T await() {
        barrier.traverse();
        T current = content;
        tryThrowException();
        return isNotNull(current, () -> "A FutureValue should never contain null");
    }

    private void tryThrowException() {
        Throwable exception = throwable;

        if (exception != null) {
            throw new IllegalStateException(new ExecutionException(exception));
        }
    }

    @Override
    public String toString() {
        return "FutureValue{" +
                "content=" + content +
                '}';
    }
}
