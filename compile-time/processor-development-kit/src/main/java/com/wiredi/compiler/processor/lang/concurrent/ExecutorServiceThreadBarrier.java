package com.wiredi.compiler.processor.lang.concurrent;

import com.wiredi.compiler.errors.CompositeProcessingException;
import com.wiredi.compiler.errors.ProcessingException;
import com.wiredi.compiler.logger.slf4j.CompileTimeLogger;
import com.wiredi.compiler.logger.slf4j.CompileTimeLoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

public class ExecutorServiceThreadBarrier implements ThreadBarrier {

	private static final CompileTimeLogger logger = CompileTimeLoggerFactory.getLogger(ExecutorServiceThreadBarrier.class);
	private final List<ContextRunnable> runnableList;
	private final ExecutorService executorService;
	private final int initialSemaphoreCount;

	public ExecutorServiceThreadBarrier(ExecutorService executorService, List<ContextRunnable> runnableList) {
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

	@Override
	public int getInitialSemaphoreCount() {
		return initialSemaphoreCount;
	}

	@Override
	public int countNotFinishedThreads() {
		return runnableList.size();
	}

	@Override
	public List<ContextRunnable> getRunnableList() {
		return runnableList;
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	@Override
	public void run() {
		if (runnableList.isEmpty()) {
			logger.trace(() -> "ThreadBarrier is empty");
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
						logger.trace(() -> "Started Thread Barrier Cycle");
						runnable.run();
						logger.trace(() -> "Finished successfully");
					} catch (CompositeProcessingException multipleProcessingException) {
						multipleProcessingException.getExceptions().forEach(processingException -> {
							logger.error(processingException.getElement(), processingException::getMessage);
						});
						runnable.onError(multipleProcessingException);
					} catch(ProcessingException processingException) {
						logger.error(processingException.getElement(), processingException::getMessage);
						runnable.onError(processingException);
					} catch (Throwable throwable) {
						logger.error(() -> "Encountered " + throwable.getClass().getSimpleName() + ":" + throwable.getMessage());
						runnable.onError(throwable);
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
				logger.error("Process has been interrupted before all Threads where completed", e);
			}
		} finally {
			currentRunnableList.clear();
			runnableList.clear();
		}
	}
}
