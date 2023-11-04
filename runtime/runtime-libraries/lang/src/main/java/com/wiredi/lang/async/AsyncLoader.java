package com.wiredi.lang.async;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AsyncLoader {

	private static final String KEY = "wiredi.eager-loading.threads";
	private static final int threads;
	static {
		String raw = System.getenv().getOrDefault(KEY, System.getProperty(KEY));
		if (raw == null) {
			threads = Runtime.getRuntime().availableProcessors();
		} else {
			try {
				threads = Integer.parseInt(raw);
			} catch (NumberFormatException t) {
				final Logger LOGGER = LoggerFactory.getLogger(AsyncLoader.class);
				LOGGER.error("Invalid value for property {}: Should be an INT", KEY);
				LOGGER.error("Unexpected exception", t);
				throw new IllegalArgumentException("The property " + KEY + " must be a number!", t);
			}
		}
	}

	private static final ExecutorService executorService = Executors.newFixedThreadPool(threads, new DaemonThreadFactory());

	public static <T> void load(Supplier<T> supplier, Consumer<T> resultConsumer) {
		executorService.execute(() -> resultConsumer.accept(supplier.get()));
	}

	public static <T> CompletionStage<T> load(Supplier<T> supplier) {
		CompletableFuture<T> completableFuture = new CompletableFuture<>();
		load(supplier, completableFuture::complete);
		return completableFuture;
	}

	public static class DaemonThreadFactory implements ThreadFactory {

		private final AtomicInteger count = new AtomicInteger(0);

		@Override
		public Thread newThread(@NotNull Runnable r) {
			String name = "eager-loading-thread-" + count.incrementAndGet();
			Thread thread = new Thread(r, name);
			thread.setDaemon(true);
			return thread;
		}
	}
}
