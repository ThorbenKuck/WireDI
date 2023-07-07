package com.wiredi.lang.async;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class FutureValue<T> {

	/**
	 * This will support VirtualThreads as soon as OpenJDK 21 has general availability.
	 */
	private static final ExecutorService executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	private final CompletableFuture<T> future;

	public FutureValue() {
		this(new CompletableFuture<>());
	}

	public FutureValue(CompletableFuture<T> future) {
		this.future = future;
	}

	public static <T> FutureValue<T> of(T t) {
		return new FutureValue<>(CompletableFuture.completedFuture(t));
	}

	public static <T> FutureValue<T> of(Supplier<T> supplier) {
		CompletableFuture<T> completableFuture = new CompletableFuture<>();
		executors.submit(() -> completableFuture.complete(supplier.get()));
		return new FutureValue<>(completableFuture);
	}

	public static FutureValue<Void> run(Runnable runnable) {
		return of(() -> {
			runnable.run();
			return null;
		});
	}

	public void set(T value) {
		if (isAvailable()) {
			throw new IllegalStateException("This future value is already completed");
		}
		future.complete(value);
	}

	public boolean isAvailable() {
		return future.isDone();
	}

	@Nullable
	public Optional<T> safeGet() {
		try {
			return Optional.ofNullable(future.get());
		} catch (ExecutionException | InterruptedException e) {
			return Optional.empty();
		}
	}

	@Nullable
	public Optional<T> get() throws ExecutionException, InterruptedException {
		return Optional.ofNullable(future.get());
	}
}
