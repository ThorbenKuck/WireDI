package com.wiredi.lang.async;

import java.util.concurrent.Semaphore;

public class Barrier {

	private final Semaphore semaphore = new Semaphore(0);
	private boolean isOpen = false;

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
}
