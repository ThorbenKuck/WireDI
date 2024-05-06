package com.wiredi.runtime.async.fences;

import com.wiredi.logging.Logging;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SynchronizedFence implements Fence {

    private final Runnable runnable;
    private final Lock lock;
    private final Object key;

    private static final Logging logger = Logging.getInstance(SynchronizedFence.class);
    private static final Map<Object, Lock> locks = new HashMap<>();

    public static SynchronizedFence local(Runnable runnable) {
        return new SynchronizedFence(runnable);
    }

    public static SynchronizedFence global(Object key, Runnable runnable) {
        return new SynchronizedFence(runnable, locks.computeIfAbsent(key, it -> new ReentrantLock()), key);
    }

    public SynchronizedFence(Runnable runnable) {
        this(runnable, new ReentrantLock(), SynchronizedFence.class);
    }

    public SynchronizedFence(Runnable runnable, Lock lock, Object key) {
        this.runnable = runnable;
        this.lock = lock;
        this.key = key;
    }

    @Override
    public void pass() {
        try {
            lock.lock();
            logger.trace("Fence(" + key + ") passed");
            runnable.run();
        } finally {
            lock.unlock();
        }
    }
}
