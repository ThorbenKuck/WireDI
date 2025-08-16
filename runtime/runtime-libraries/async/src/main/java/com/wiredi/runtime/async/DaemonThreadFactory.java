package com.wiredi.runtime.async;

import com.wiredi.logging.Logging;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;

public record DaemonThreadFactory(int priority) implements ThreadFactory {

    private static final Logging logger = Logging.getInstance(DaemonThreadFactory.class);

    public DaemonThreadFactory() {
        this(Thread.MIN_PRIORITY + 1);
    }

    @Override
    public Thread newThread(@NotNull Runnable r) {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setPriority(priority);
        logger.trace(() -> "Constructed new daemon thread: " + thread.getName());
        return thread;
    }
}
