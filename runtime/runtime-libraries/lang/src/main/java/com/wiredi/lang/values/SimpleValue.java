package com.wiredi.lang.values;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static com.wiredi.lang.Preconditions.notNull;

public class SimpleValue<T> implements Value<T> {

    @Nullable
    private T t;

    public SimpleValue(@Nullable T t) {
        this.t = t;
    }

    public SimpleValue() {
        this.t = null;
    }

    @Override
    public @NotNull T get() {
        return notNull(t, () -> "Value was empty");
    }

    @Override
    public void set(@NotNull T t) {
        this.t = t;
    }

    @Override
    public boolean isSet() {
        return t != null;
    }

    @Override
    public void ifEmpty(@NotNull Runnable runnable) {
        T current = t;

        if (current == null) {
            runnable.run();
        }
    }

    @Override
    public @NotNull IfPresentStage ifPresent(@NotNull Consumer<T> presentConsumer) {
        T current = t;

        if (current != null) {
            presentConsumer.accept(current);
            return IfPresentStage.wasPresent();
        } else {
            return IfPresentStage.wasMissing();
        }
    }
}
