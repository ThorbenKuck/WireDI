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
 * A Value implementation that asynchronously loads its content.
 * <p>
 * This implementation listens to a {@link CompletionStage} and updates its state
 * when the linked CompletionStage is completed. It provides methods to check if
 * the value is available, wait for the value to become available, and handle
 * exceptions that might occur during the asynchronous loading.
 * <p>
 * FutureValue is particularly useful for values that are expensive to compute
 * and can be loaded in the background while other operations continue. When the
 * value is requested before it's available, the calling thread will block until
 * the value is ready.
 *
 * @param <T> The type of value stored in this container
 * @see Value#async(ThrowingSupplier)
 * @see CompletionStage
 * @see AsyncLoader
 */
public class FutureValue<T> implements Value<T> {

    private final SemaphoreBarrier barrier = SemaphoreBarrier.closed();
    @Nullable
    private CompletionStageSubscription<T> subscription;
    private T content;
    private Throwable throwable;

    /**
     * Creates a new FutureValue that listens to the specified CompletionStage.
     * <p>
     * The value will be updated when the CompletionStage completes, either with
     * a value or an exception.
     *
     * @param completionStage The CompletionStage to listen to
     */
    public FutureValue(CompletionStage<@NotNull T> completionStage) {
        this.subscription = newSubscription(completionStage);
    }

    /**
     * Creates a new FutureValue with the specified value.
     * <p>
     * This factory method creates a FutureValue that is immediately available
     * with the provided value.
     *
     * @param t   The value to use
     * @param <T> The type of the value
     * @return A new FutureValue containing the provided value
     */
    public static <T> FutureValue<@NotNull T> of(@Nullable T t) {
        return new FutureValue<>(CompletableFuture.completedFuture(t));
    }

    /**
     * Creates a new FutureValue that asynchronously loads its value using the provided supplier.
     * <p>
     * This factory method creates a FutureValue that will load its value asynchronously
     * using the AsyncLoader. The supplier will be executed in a separate thread or fiber,
     * and the FutureValue will be updated when the supplier completes.
     *
     * @param supplier The supplier that will provide the value
     * @param <T>      The type of the value
     * @return A new FutureValue that will be updated when the supplier completes
     * @see AsyncLoader#load(ThrowingSupplier)
     */
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

    /**
     * Checks if the value is available.
     * <p>
     * This method returns true if the CompletionStage has completed and the value
     * is available, or false if the CompletionStage is still running.
     *
     * @return true if the value is available, false otherwise
     */
    public boolean isAvailable() {
        return barrier.isOpen();
    }

    /**
     * Gets the current value, waiting if necessary.
     * <p>
     * This method blocks until the value is available, then returns it.
     * If the CompletionStage completed with an exception, that exception is thrown.
     *
     * @return The current value
     * @throws InterruptedException If the thread is interrupted while waiting
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
     * <p>
     * This implementation blocks until the value is available. If the CompletionStage
     * completed with an exception, that exception is wrapped in an IllegalStateException
     * and thrown.
     *
     * @throws IllegalStateException If the CompletionStage completed with an exception
     *                               or if the thread is interrupted while waiting
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

    /**
     * Gets the current value, waiting up to the specified duration if necessary.
     * <p>
     * This method blocks until the value is available or the duration expires,
     * then returns the value. If the CompletionStage completed with an exception,
     * that exception is thrown.
     *
     * @param duration The maximum time to wait
     * @return The current value
     * @throws InterruptedException If the thread is interrupted while waiting
     */
    public T get(Duration duration) throws InterruptedException {
        barrier.traverse(duration);
        T current = content;
        tryThrowException();

        return current;
    }

    /**
     * Sets the value or exception directly, bypassing the CompletionStage.
     * <p>
     * This method is used internally to update the value when the CompletionStage completes.
     * It can also be used to manually set the value or exception.
     *
     * @param content   The value to set, may be null if throwable is not null
     * @param throwable The exception to set, may be null if content is not null
     * @throws IllegalArgumentException If both content and throwable are null
     */
    public void unsafeSet(
            @Nullable T content,
            @Nullable Throwable throwable
    ) {
        is(content != null || throwable != null, () -> "Either the content, or the exception need top be provided");
        this.content = content;
        this.throwable = throwable;
        this.barrier.open();
    }

    /**
     * Cancels the current subscription to the CompletionStage.
     * <p>
     * This method cancels the current subscription if one exists, preventing
     * the value from being updated when the CompletionStage completes.
     */
    public void cancelCurrentSubscription() {
        CompletionStageSubscription<T> currentSubscription = this.subscription;
        if (currentSubscription != null) {
            currentSubscription.cancel();
            this.subscription = null;
        }
    }

    /**
     * Sets a new CompletionStage to listen to.
     * <p>
     * This method cancels the current subscription, resets the value, and
     * creates a new subscription to the specified CompletionStage.
     *
     * @param completionStage The new CompletionStage to listen to
     */
    public void set(@NotNull CompletionStage<@NotNull T> completionStage) {
        cancelCurrentSubscription();
        this.barrier.close();
        this.content = null;
        this.throwable = null;
        this.subscription = newSubscription(completionStage);
    }

    /**
     * Sets the value directly and cancels any current subscription.
     * <p>
     * This method cancels the current subscription and sets the value directly,
     * making it immediately available.
     *
     * @param t The value to set
     */
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
     * Waits for the value to become available and returns it.
     * <p>
     * This method blocks until the value is available, then returns it.
     * If the CompletionStage completed with an exception, that exception is thrown.
     * <p>
     * Unlike {@link #get()}, this method does not catch InterruptedException, allowing
     * it to be propagated to the caller.
     *
     * @return The value, never null
     * @throws IllegalStateException If the CompletionStage completed with an exception
     */
    @NotNull
    public T await() {
        barrier.traverse();
        T current = content;
        tryThrowException();
        return isNotNull(current, () -> "A FutureValue should never contain null");
    }

    /**
     * Throws an exception if the CompletionStage completed with an exception.
     * <p>
     * This method is used internally to check if the CompletionStage completed with
     * an exception, and if so, throw that exception wrapped in an IllegalStateException.
     *
     * @throws IllegalStateException If the CompletionStage completed with an exception
     */
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
