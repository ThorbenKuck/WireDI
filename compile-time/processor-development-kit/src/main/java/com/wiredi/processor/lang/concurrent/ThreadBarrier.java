package com.wiredi.processor.lang.concurrent;

import com.wiredi.compiler.errors.ProcessingException;
import com.wiredi.compiler.logger.Logger;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class ThreadBarrier {

	private static final Logger logger = Logger.get(ThreadBarrier.class);
	private final List<ContextRunnable> runnableList;
	private final ExecutorService executorService;
	private final int initialSemaphoreCount;

	public ThreadBarrier(ExecutorService executorService, List<ContextRunnable> runnableList) {
		if (runnableList.isEmpty()) {
			throw new IllegalArgumentException("No runnables provided. At least one runnable has to be provided for the ThreadBarrier to work");
		}
		this.executorService = executorService;
		this.runnableList = runnableList;
		this.initialSemaphoreCount = 1 - runnableList.size();
	}

	public static Builder builder() {
		return new Builder();
	}

	public int getInitialSemaphoreCount() {
		return initialSemaphoreCount;
	}

	public int countNotFinishedThreads() {
		return runnableList.size();
	}

	public List<ContextRunnable> getRunnableList() {
		return runnableList;
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public void run() {
		if (runnableList.isEmpty()) {
			logger.debug(() -> "ThreadBarrier is empty");
			return;
		}
		ArrayList<ContextRunnable> currentRunnableList = new ArrayList<>(runnableList);
		Semaphore semaphore = new Semaphore(initialSemaphoreCount);
		int requiredInvocations = -(1 - currentRunnableList.size());
		logger.debug(() -> "ThreadBarrier awaiting " + requiredInvocations);

		try {
			currentRunnableList.forEach(runnable -> {
				executorService.submit(() -> {
					runnable.beforeEach();
					try {
						logger.debug(() -> "Started Thread Barrier Cycle");
						runnable.run();
						logger.debug(() -> "Finished successfully");
					} catch(ProcessingException processingException) {
						logger.error(processingException.getElement(), processingException::getMessage);
						runnable.onError(processingException);
					} catch (Throwable throwable) {
//						logger.error(() -> "Encountered " + throwable.getClass().getSimpleName() + ":" + throwable.getMessage());
//						runnable.onError(throwable);
					} finally {
						try {
							runnable.afterEach();
						} finally {
							runnableList.remove(runnable);
							semaphore.release();
						}
					}
				});
			});
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				logger.catching(e);
			}
		} finally {
			currentRunnableList.clear();
			runnableList.clear();
		}
	}

	public static class Builder {

		private final List<ContextRunnable> runnableList = new ArrayList<>();
		private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		public Builder withExecutorService(ExecutorService executorService) {
			this.executorService = executorService;
			return this;
		}

		public Builder withRunnable(ContextRunnable first) {
			return withRunnables(first);
		}

		public Builder withRunnables(ContextRunnable first, ContextRunnable... other) {
			this.runnableList.add(first);
			this.runnableList.addAll(Arrays.asList(other));
			return this;
		}

		public Builder withRunnables(Collection<ContextRunnable> collection) {
			this.runnableList.addAll(collection);
			return this;
		}

		public void run() {
			ThreadBarrier build = build();
			build.run();
		}

		public ThreadBarrier build() {
			return new ThreadBarrier(executorService, runnableList);
		}
	}
}
