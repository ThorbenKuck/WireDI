package com.wiredi.lang.async;

import java.time.Duration;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class Barrier {

	private final Semaphore semaphore = new Semaphore(0);
	private boolean isOpen = false;

	public static Barrier opened() {
		Barrier barrier = new Barrier();
		barrier.open();
		return barrier;
	}

	public static Barrier closed() {
		Barrier barrier = new Barrier();
		barrier.close();
		return barrier;
	}

	public void open() {
		if (isOpen) {
			return;
		}
		isOpen = true;
		semaphore.release();
	}

	public void close() {
		if (!isOpen) {
			return;
		}
		isOpen = false;
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			throw new AsyncBarrierException(e);
		}
	}

	public void traverse() {
		if (!isOpen) {
			try {
				semaphore.acquire();
				semaphore.release();
			} catch (InterruptedException e) {
				throw new AsyncBarrierException(e);
			}
		}
	}

	public void traverse(Duration duration) {
		if (!isOpen) {
			try {
				if (!semaphore.tryAcquire(duration.getNano(), TimeUnit.NANOSECONDS)) {
					throw new IllegalStateException("Could not traverse the barrier");
				}
				semaphore.release();
			} catch (InterruptedException e) {
				throw new AsyncBarrierException(e);
			}
		}
	}

	public <T> T traverseAndGet(Supplier<T> supplier) {
		T t;
		try {
			semaphore.acquire();
			t = supplier.get();
			semaphore.release();
		} catch (InterruptedException e) {
			throw new AsyncBarrierException(e);
		}
		return t;
	}

	public <T> T traverseAndGet(Duration duration, Supplier<T> supplier) {
		T t;
		try {
			if (!semaphore.tryAcquire(duration.getNano(), TimeUnit.NANOSECONDS)) {
				throw new IllegalStateException("Could not traverse the barrier");
			}
			t = supplier.get();
			semaphore.release();
		} catch (InterruptedException e) {
			throw new AsyncBarrierException(e);
		}
		return t;
	}
}
