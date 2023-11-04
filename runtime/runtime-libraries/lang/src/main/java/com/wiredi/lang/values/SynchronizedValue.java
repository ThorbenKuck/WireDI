package com.wiredi.lang.values;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.wiredi.lang.Preconditions.notNull;

public class SynchronizedValue<T> implements Value<T> {

    @NotNull
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    @Nullable
    private T content;

    public SynchronizedValue(@Nullable T content) {
        this.content = content;
    }

    public SynchronizedValue() {
        this.content = null;
    }

    public <S> S read(Function<T, S> consumer) {
        Lock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            return consumer.apply(content);
        } finally {
            readLock.unlock();
        }
    }

    public void write(Supplier<@NotNull T> supplier) {
        Lock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            this.content = supplier.get();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public @NotNull T get() {
        return read(current -> notNull(current, () -> "The value contained null, this is not allowed"));
    }

    @Override
    public void set(@NotNull T t) {
        write(() -> t);
    }

    @Override
    public boolean isSet() {
        return read(Objects::nonNull);
    }

    @Override
    public void ifEmpty(@NotNull Runnable runnable) {
        T current = read(it -> it);

        if (current == null) {
            runnable.run();
        }
    }

    @Override
    public @NotNull IfPresentStage ifPresent(@NotNull Consumer<T> presentConsumer) {
        T current = read(it -> it);

        if (current != null) {
            presentConsumer.accept(current);
            return IfPresentStage.wasPresent();
        } else {
            return IfPresentStage.wasMissing();
        }
    }
}
