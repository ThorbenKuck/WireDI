package com.wiredi.runtime;

import com.wiredi.runtime.lang.ThrowingRunnable;
import com.wiredi.runtime.lang.ThrowingSupplier;
import com.wiredi.runtime.time.Timed;
import com.wiredi.runtime.time.TimedValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;

public class StartupDiagnostics {

    @NotNull
    private final StartupDiagnostics.TimingState root = new TimingState("root");
    private boolean sealed = false;
    private StartupDiagnostics.TimingState pointer = root;

    public TimingState state() {
        return pointer;
    }

    public <T extends Throwable> Timed measure(String name, ThrowingRunnable<T> runnable) throws T {
        Timed duration;
        try {
            start(name);
            runnable.run();
        } finally {
            duration = stop();
        }
        return duration;
    }

    public <T extends Throwable, S> TimedValue<S> measure(String name, ThrowingSupplier<S, T> runnable) throws T {
        Timed duration;
        S result;
        try {
            start(name);
            result = runnable.get();
        } finally {
            duration = stop();
        }
        return new TimedValue<>(result, duration);
    }

    private void start(String name) {
        checkSealed();
        this.pointer = pointer.nest(name);
        this.pointer.start();
    }

    private Timed stop() {
        checkSealed();
        if (pointer == root) {
            seal();
        }
        Timed duration = this.pointer.stop();
        this.pointer = pointer.unwrap();
        return duration;
    }

    public void seal() {
        sealed = true;
    }

    public void reset() {
        sealed = false;
        root.reset();
    }

    private void checkSealed() {
        if (sealed) {
            throw new IllegalStateException("Cannot add timings to a sealed StartupDiagnostics instance. Make sure to add the diagnostics before the WireRepository is loaded completely.");
        }
    }

    public static class TimingState {

        private final String name;
        private final Map<String, TimingState> children = new HashMap<>();
        @Nullable
        private final StartupDiagnostics.TimingState previous;
        private Timed total = Timed.of(Duration.ZERO);
        private Long start;


        private TimingState(String name, @Nullable StartupDiagnostics.TimingState previous) {
            this.name = name;
            this.previous = previous;
        }

        private TimingState(String name) {
            this(name, null);
        }

        public boolean isEmpty() {
            return children.isEmpty();
        }

        public Map<String, TimingState> mapChildren() {
            return Collections.unmodifiableMap(children);
        }

        public Collection<TimingState> children() {
            return children.values();
        }

        public String name() {
            return name;
        }

        public Timed time() {
            return total;
        }

        public void reset() {
            this.start = null;
            this.total = Timed.of(Duration.ZERO);
            this.children.values().forEach(TimingState::reset);
            this.children.clear();
        }

        public void start() {
            if (start != null) {
                throw new IllegalStateException("The timing is already running. Call stop() before calling start() again.");
            }
            start = System.currentTimeMillis();
        }

        public Timed stop() {
            if (start == null) {
                throw new IllegalStateException("The timing has already been stopped. Call start() before calling stop() again.");
            }
            Duration increment = Duration.ofMillis(System.currentTimeMillis() - start);
            start = null;
            total = total.plus(increment);
            return Timed.of(increment);
        }

        @NotNull
        public TimingState nest(String name) {
            return children.computeIfAbsent(name, (k) -> new TimingState(k, this));
        }

        @Nullable
        public TimingState getChild(String name) {
            return children.get(name);
        }

        @NotNull
        public StartupDiagnostics.TimingState unwrap() {
            return Objects.requireNonNullElse(previous, this);
        }
    }
}
