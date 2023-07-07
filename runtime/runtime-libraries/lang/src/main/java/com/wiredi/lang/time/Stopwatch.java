package com.wiredi.lang.time;

import com.wiredi.lang.Preconditions;

public class Stopwatch {

	private long startTick = -1;
	private long elapsedNanos = 0;

	public static Stopwatch started() {
		Stopwatch stopwatch = new Stopwatch();
		stopwatch.start();

		return stopwatch;
	}

	public static Stopwatch of(Runnable runnable) {
		Stopwatch stopwatch = new Stopwatch();

		return stopwatch.run(runnable);
	}

	public final long elapsed() {
		return elapsedNanos;
	}

	public final long reset() {
		long elapsed = elapsedNanos;
		if (isStarted()) {
			this.startTick = tick();
		} else {
			this.elapsedNanos = 0;
		}
		return elapsed;
	}

	public Stopwatch start() {
		Preconditions.require(!isStarted(), () -> "The stopwatch is already started");
		this.startTick = tick();
		return this;
	}

	public Stopwatch stop() {
		Preconditions.require(isStarted(), () -> "The must be started");
		this.elapsedNanos += tick() - this.startTick;
		this.startTick = -1;
		return this;
	}

	public Stopwatch run(Runnable runnable) {
		if (!isStarted()) {
			start();
			runnable.run();
			stop();
		} else {
			runnable.run();
		}
		return this;
	}

	public boolean isStarted() {
		return startTick >= 0;
	}

	private long tick() {
		return System.nanoTime();
	}
}
