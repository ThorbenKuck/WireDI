package com.github.thorbenkuck.di;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

public class DataAccess {

    @NotNull
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    @NotNull
    public <T> T read(@NotNull final Supplier<T> supplier) {
        Lock lock = readWriteLock.readLock();
        try {
            lock.lock();
            T t = supplier.get();
            return Objects.requireNonNull(t);
        } finally {
            lock.unlock();
        }
    }

    public void write(@NotNull final Runnable runnable) {
        runLocked(runnable, readWriteLock.writeLock());
    }

    private void runLocked(
            @NotNull final Runnable runnable,
            @NotNull final Lock lock
    ) {
        try {
            lock.lock();
            runnable.run();
        } finally {
            lock.unlock();
        }
    }
}
