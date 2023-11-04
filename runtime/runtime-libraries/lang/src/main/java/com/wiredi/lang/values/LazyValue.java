package com.wiredi.lang.values;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class LazyValue<T> implements Value<T> {

    private Supplier<T> supplier;
    private T content;

    public LazyValue(@NotNull Supplier<@NotNull T> supplier) {
        this.supplier = supplier;
    }

    private T getCurrent() {
        if (content == null) {
            content = supplier.get();
        }

        return content;
    }

    @Override
    @NotNull
    public T get() {
        return getCurrent();
    }

    @Override
    public void set(@NotNull T t) {
        this.content = t;
        this.supplier = null;
    }

    public void set(@NotNull Supplier<T> supplier) {
        this.supplier = supplier;
        this.content = null;
    }

    @Override
    public boolean isSet() {
        return getCurrent() != null;
    }

    @Override
    public void ifEmpty(@NotNull Runnable runnable) {
        T content = this.content;

        if (content == null) {
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
}
