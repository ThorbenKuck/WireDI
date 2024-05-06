package com.wiredi.runtime.async;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * This class is a utility wrapper around a {@link ReadWriteLock}.
 * <p>
 * It holds two kind of methods: with and without return values.
 * Methods ending on "Value", will return the provided value, whilst methods without it will not return anything.
 * <p>
 * Example usage:
 *
 * <pre><code>
 * public class MyDomain {
 *     private Entity content;
 *     private final DataAccess dataAccess = new DataAccess();
 *
 *     public void updateEntity(Entity entity) {
 *         dataAccess.write(() -> this.content = entity);
 *     }
 *
 *     public Entity getEntity() {
 *         return dataAccess.readValue(() -> this.content);
 *     }
 * }
 * </code></pre>
 * <p>
 * "Value" methods will not support returned null values.
 * To support nullable return values, use methods ending on "NullableValue"
 */
public final class DataAccess {

    @NotNull
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public void read(@NotNull final Runnable supplier) {
        @NotNull final Lock lock = readWriteLock.readLock();
        try {
            lock.lock();
            supplier.run();
        } finally {
            lock.unlock();
        }
    }

    @NotNull
    public <T> T readValue(@NotNull final Supplier<@NotNull T> supplier) {
        @NotNull final Lock lock = readWriteLock.readLock();
        try {
            lock.lock();
            return Objects.requireNonNull(supplier.get());
        } finally {
            lock.unlock();
        }
    }

    @Nullable
    public <T> T readNullableValue(@NotNull final Supplier<@Nullable T> supplier) {
        @NotNull final Lock lock = readWriteLock.readLock();
        try {
            lock.lock();
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }

    public void write(@NotNull final Runnable runnable) {
        @NotNull final Lock lock = readWriteLock.writeLock();
        try {
            lock.lock();
            runnable.run();
        } finally {
            lock.unlock();
        }
    }

    @NotNull
    public <T> T writeValue(@NotNull final Supplier<@NotNull T> supplier) {
        @NotNull final Lock lock = readWriteLock.writeLock();
        try {
            lock.lock();
            return Objects.requireNonNull(supplier.get());
        } finally {
            lock.unlock();
        }
    }

    @Nullable
    public <T> T writeNullableValue(@NotNull final Supplier<@Nullable T> supplier) {
        @NotNull final Lock lock = readWriteLock.writeLock();
        try {
            lock.lock();
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }
}
