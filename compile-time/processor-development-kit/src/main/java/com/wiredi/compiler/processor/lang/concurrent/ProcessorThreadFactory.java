package com.wiredi.compiler.processor.lang.concurrent;

import com.wiredi.runtime.lang.Counter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;

import static java.lang.Thread.MAX_PRIORITY;

public final class ProcessorThreadFactory implements ThreadFactory {

    private final Counter counter = new Counter(0);
    private final ThreadGroup group;
    private final ClassLoader classLoader;
    private final int priority;

    public ProcessorThreadFactory() {
        group = Thread.currentThread().getThreadGroup();
        classLoader = Thread.currentThread().getContextClassLoader();
        priority = MAX_PRIORITY;
    }

    @Override
    public Thread newThread(@NotNull Runnable r) {
        Thread thread = new Thread(group, r, "Processor-" + counter.increment());
        thread.setContextClassLoader(classLoader);
        thread.setPriority(priority);
        return thread;
    }
}
