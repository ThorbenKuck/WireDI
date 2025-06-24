package com.wiredi.processor.lang.concurrent;

import com.wiredi.compiler.processor.lang.concurrent.ContextRunnable;
import com.wiredi.compiler.processor.lang.concurrent.ExecutorServiceThreadBarrier;
import com.wiredi.compiler.processor.lang.concurrent.ThreadBarrier;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

class ThreadBarrierTest {
	@Test
	public void test() {
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		Result result = new Result();
		ThreadBarrier barrier = ExecutorServiceThreadBarrier.builder()
				.withExecutorService(executorService)
				.withRunnables(new TestRunnable(result))
				.withRunnables(new TestRunnable(result))
				.withRunnables(new TestRunnable(result))
				.withRunnables(new TestRunnable(result))
				.withRunnables(new TestRunnable(result))
				.withRunnables(new TestRunnable(result))
				.build();

		assertThat(barrier.getRunnableList()).hasSize(6);
		assertThat(barrier.countNotFinishedThreads()).isEqualTo(6);
		assertThat(barrier.getInitialSemaphoreCount()).isEqualTo(-5);
		barrier.run();
		assertThat(result.getI()).isEqualTo(6);
		assertThat(barrier.getRunnableList()).hasSize(0);
		assertThat(barrier.getInitialSemaphoreCount()).isEqualTo(-5);
		assertThat(barrier.countNotFinishedThreads()).isEqualTo(0);
	}
	private static class Result {
		private int i = 0;

		public void increment() {
			synchronized (this) {
				i += 1;
			}
		}

		public int getI() {
			synchronized (this) {
				return i;
			}
		}
	}

	private record TestRunnable(Result result) implements ContextRunnable {

		@Override
		public void run() {
			try {
				Thread.sleep(100);
				result.increment();
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
		}
	}
}