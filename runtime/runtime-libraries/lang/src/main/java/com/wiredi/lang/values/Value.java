package com.wiredi.lang.values;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface Value<T> {
    @NotNull
    static <T> Value<@NotNull T> async(@NotNull Supplier<@NotNull T> supplier) {
        return FutureValue.of(supplier);
    }

    @NotNull
    static <T> Value<@NotNull T> lazy(@NotNull Supplier<@NotNull T> supplier) {
        return new LazyValue<>(supplier);
    }

    @NotNull
    static <T> Value<@NotNull T> neverNull(@NotNull T t) {
        return new NeverNullValue<>(t);
    }

    @NotNull
    static <T> Value<@NotNull T> synchronize(@NotNull T t) {
        return new SynchronizedValue<>(t);
    }

    @NotNull
    static <T> Value<@NotNull T> just(@NotNull T t) {
        return new SimpleValue<>(t);
    }

    @NotNull
    static <T> Value<@NotNull T> empty() {
        return new SimpleValue<>();
    }

    /**
     * Returns the content of the value.
     * <p>
     * As null values are not allowed, this method must throw a {@link NullPointerException}, if the content is null.
     *
     * @return the content of the value
     * @throws NullPointerException if the value is null
     */
    @NotNull
    T get();

    void set(@NotNull T t);

    boolean isSet();

    default boolean isEmpty() {
        return !isSet();
    }

    void ifEmpty(@NotNull Runnable runnable);

    @NotNull
    IfPresentStage ifPresent(@NotNull Consumer<T> presentConsumer);

    @NotNull
    default Supplier<T> asSupplier() {
        return this::get;
    }

}
