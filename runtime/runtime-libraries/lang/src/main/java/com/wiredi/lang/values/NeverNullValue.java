package com.wiredi.lang.values;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class NeverNullValue<T> implements Value<T> {

    @NotNull
    private T content;

    public NeverNullValue(@NotNull T content) {
        this.content = content;
    }

    @Override
    public void set(@NotNull T t) {
        this.content = t;
    }

    @Override
    public boolean isSet() {
        return true;
    }

    @Override
    public void ifEmpty(@NotNull Runnable runnable) {
        // This cannot happen, has this value always has to be set
    }

    @Override
    public @NotNull IfPresentStage ifPresent(@NotNull Consumer<T> presentConsumer) {
        presentConsumer.accept(content);
        return IfPresentStage.wasPresent();
    }

    public void trySet(@Nullable T t) {
        if (t != null) {
            this.content = t;
        }
    }

    @NotNull
    @Override
    public T get() {
        return this.content;
    }
}
