package com.wiredi.lang.async;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public class State<T> {

	private final Barrier barrier;
	private T t;

	public State(@Nullable T t) {
		this.t = t;
		if (t != null) {
			barrier = Barrier.opened();
		} else {
			barrier = Barrier.closed();
		}
	}

	public State() {
		this.t = null;
		barrier = Barrier.closed();
	}

	@NotNull
	public T get() {
		return barrier.traverseAndGet(() -> t);
	}

	@NotNull
	public T get(Duration duration) {
		return barrier.traverseAndGet(duration, () -> t);
	}

	public void awaitUntilSet() {
		barrier.traverse();
	}

	public void awaitUntilSet(Duration duration) {
		barrier.traverse(duration);
	}

	public synchronized void set(@NotNull T t) {
		if (this.t != null) {
			throw new IllegalStateException("The state cannot be updated");
		}
		this.t = t;
		barrier.open();
	}

	public synchronized void clear() {
		this.t = null;
		barrier.close();
	}
}
