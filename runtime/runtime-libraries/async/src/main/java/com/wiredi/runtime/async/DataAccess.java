package com.wiredi.runtime.async;

import com.wiredi.logging.Logging;
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

    private static final Logging logger = Logging.getInstance(DataAccess.class);

    public void read(@NotNull final Runnable supplier) {
        logger.debug("Reading data");
        @NotNull final Lock lock = readWriteLock.readLock();
        try {
            logger.trace("Acquiring lock");
            lock.lock();
            logger.trace("Lock acquired");
            supplier.run();
        } finally {
            lock.unlock();
            logger.trace("Lock released");
        }
    }

    @NotNull
    public <T> T readValue(@NotNull final Supplier<@NotNull T> supplier) {
        logger.debug("Reading data");
        @NotNull final Lock lock = readWriteLock.readLock();
        try {
            logger.trace("Acquiring lock");
            lock.lock();
            logger.trace("Lock acquired");
            return Objects.requireNonNull(supplier.get());
        } finally {
            lock.unlock();
            logger.trace("Lock released");
        }
    }

    @Nullable
    public <T> T readNullableValue(@NotNull final Supplier<@Nullable T> supplier) {
        logger.debug("Reading data");
        @NotNull final Lock lock = readWriteLock.readLock();
        try {
            logger.trace("Acquiring lock");
            lock.lock();
            logger.trace("Lock acquired");
            return supplier.get();
        } finally {
            lock.unlock();
            logger.trace("Lock released");
        }
    }

    public void write(@NotNull final Runnable runnable) {
        logger.debug("Writing data");
        @NotNull final Lock lock = readWriteLock.writeLock();
        try {
            logger.trace("Acquiring lock");
            lock.lock();
            logger.trace("Lock acquired");
            runnable.run();
        } finally {
            lock.unlock();
            logger.trace("Lock released");
        }
    }

    @NotNull
    public <T> T writeValue(@NotNull final Supplier<@NotNull T> supplier) {
        logger.debug("Writing data");
        @NotNull final Lock lock = readWriteLock.writeLock();
        try {
            logger.trace("Acquiring lock");
            lock.lock();
            logger.trace("Lock acquired");
            return Objects.requireNonNull(supplier.get());
        } finally {
            lock.unlock();
            logger.trace("Lock released");
        }
    }

    @Nullable
    public <T> T writeNullableValue(@NotNull final Supplier<@Nullable T> supplier) {
        logger.debug("Writing data");
        @NotNull final Lock lock = readWriteLock.writeLock();
        try {
            logger.trace("Acquiring lock");
            lock.lock();
            logger.trace("Lock acquired");
            return supplier.get();
        } finally {
            lock.unlock();
            logger.trace("Lock released");
        }
    }
}
