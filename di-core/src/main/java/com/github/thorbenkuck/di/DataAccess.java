package com.github.thorbenkuck.di;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

public class DataAccess {

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public <T>T read(Supplier<T> supplier) {
        Lock lock = readWriteLock.readLock();
        try {
            lock.lock();
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }

    public void write(Runnable runnable) {
        runLocked(runnable, readWriteLock.writeLock());
    }

    private void runLocked(Runnable runnable, Lock lock) {
        try {
            lock.lock();
            runnable.run();
        } finally {
            lock.unlock();
        }
    }
}
