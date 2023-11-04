package com.wiredi.processor.lang.concurrent;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.MAX_PRIORITY;

public final class ProcessorThreadFactory implements ThreadFactory {

	private final AtomicInteger atomicInteger = new AtomicInteger(0);
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
		Thread thread = new Thread(group, r, "Processor-" + atomicInteger.incrementAndGet());
		thread.setContextClassLoader(classLoader);
		thread.setPriority(priority);
		return thread;
	}
}
