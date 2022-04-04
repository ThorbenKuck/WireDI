package com.github.thorbenkuck.di.processor.concurrent;

import com.github.thorbenkuck.di.processor.Logger;
import com.github.thorbenkuck.di.processor.WireInformation;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class ThreadBarrier {

	private final List<Runnable> runnableList;
	private final ExecutorService executorService;
	private final int expectedInvocations;

	public ThreadBarrier(ExecutorService executorService, List<Runnable> runnableList) {
		if(runnableList.isEmpty()) {
			throw new IllegalArgumentException("No runnables provided. At least one runnable has to be provided for the ThreadBarrier to work");
		}
		this.executorService = executorService;
		this.runnableList = runnableList;
		this.expectedInvocations = 1 - runnableList.size();
	}

	public int getExpectedInvocations() {
		return expectedInvocations;
	}

	public List<Runnable> getRunnableList() {
		return runnableList;
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public void run() {
		if(runnableList.isEmpty()) {
			Logger.debug("ThreadBarrier is empty");
			return;
		}
		Semaphore semaphore = new Semaphore(expectedInvocations);
		int requiredInvocations = -(1 - runnableList.size());
		Logger.debug("ThreadBarrier awaiting " + requiredInvocations);

		try {
			runnableList.forEach(runnable -> {
				executorService.submit(() -> {
					String prefix = "[corr=" + UUID.randomUUID()+ "]:";
					try {
						Logger.debug("%s started", prefix);
						runnable.run();
						Logger.debug("%s finished successfully", prefix);
					} catch (Exception e) {
						Logger.error("%s encountered exception", prefix);
					} finally {
						semaphore.release();
					}
				});
			});
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				Logger.catching(e);
			}
		} finally {
			runnableList.clear();
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private ExecutorService executorService = Executors.newCachedThreadPool();
		private final List<Runnable> runnableList = new ArrayList<>();

		public Builder withExecutorService(ExecutorService executorService) {
			this.executorService = executorService;
			return this;
		}

		public Builder withRunnable(Runnable first, Runnable... other) {
			this.runnableList.add(first);
			this.runnableList.addAll(Arrays.asList(other));
			return this;
		}

		public Builder withRunnables(Collection<Runnable> collection) {
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
