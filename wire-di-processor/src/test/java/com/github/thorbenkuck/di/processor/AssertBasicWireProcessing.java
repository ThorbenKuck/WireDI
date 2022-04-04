package com.github.thorbenkuck.di.processor;

import com.github.thorbenkuck.di.processor.concurrent.ThreadBarrier;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

public class AssertBasicWireProcessing {

	@Test
	public void test() {
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		Result result = new Result();
		ThreadBarrier barrier = ThreadBarrier.builder()
				.withExecutorService(executorService)
				.withRunnable(new TestRunnable(result))
				.withRunnable(new TestRunnable(result))
				.withRunnable(new TestRunnable(result))
				.withRunnable(new TestRunnable(result))
				.withRunnable(new TestRunnable(result))
				.withRunnable(new TestRunnable(result))
				.build();

		assertThat(barrier.getRunnableList()).hasSize(6);
		assertThat(barrier.getExpectedInvocations()).isEqualTo(-5);
		Logger.debug("Awaiting for the finish");
		barrier.run();
		assertThat(result.getI()).isEqualTo(6);
	}

	private class Result {
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

	private class TestRunnable implements Runnable {

		private final Result result;

		public TestRunnable(Result result) {
			this.result = result;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(1000);
				result.increment();
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
		}
	}
}
