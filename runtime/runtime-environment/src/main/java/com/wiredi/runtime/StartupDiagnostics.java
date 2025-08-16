package com.wiredi.runtime;

import com.wiredi.runtime.lang.ThrowingRunnable;
import com.wiredi.runtime.lang.ThrowingSupplier;
import com.wiredi.runtime.time.Timed;
import com.wiredi.runtime.time.TimedValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;

public class StartupDiagnostics {

    @NotNull
    private final StartupDiagnostics.TimingState root = new TimingState("root");
    private boolean sealed = false;
    private StartupDiagnostics.TimingState pointer = root;

    public TimingState state() {
        return pointer;
    }

    public <T extends Throwable> Timed measure(String name, ThrowingRunnable<T> runnable) throws T {
        if (this.pointer.name().equals(name)) {
            throw new IllegalStateException("Tried to recursively nest " + name);
        }

        try {
            return nestState(name).measure(runnable);
        } finally {
            unnest();
        }
    }

    public <T extends Throwable, S> TimedValue<S> measure(String name, ThrowingSupplier<S, T> supplier) throws T {
        if (this.pointer.name().equals(name)) {
            throw new IllegalStateException("Tried to recursively nest " + name);
        }

        try {
            return nestState(name).measure(supplier);
        } finally {
            unnest();
        }
    }

    private TimingState nestState(String name) {
        if (sealed) {
            return new TimingState(name);
        }
        if (this.pointer == root) {
            root.start();
        }
        TimingState state = pointer.nest(name);
        this.pointer = state;
        return state;
    }

    private void unnest() {
        if (sealed) {
            return;
        }
        TimingState state = pointer;
        if (state != root) {
            this.pointer = state.unwrap();
            if (pointer == root) {
                root.stop();
            }
        }
    }

    public void accept(Visitor visitor) {
        try {
            visitor.acceptRoot(root);
            for (TimingState value : root.children.values()) {
                accept(visitor, value, 1);
            }
        } finally {
            visitor.cleanup();
        }
    }

    private void accept(Visitor visitor, TimingState currentRoot, int depth) {
        for (TimingState value : currentRoot.children.values()) {
            visitor.accept(value, depth);
            if (!value.isEmpty()) {
                visitor.nest();
                accept(visitor, value, depth + 1);
                visitor.unnest();
            }
        }
    }

    public void seal() {
        sealed = true;
    }

    public void reset() {
        sealed = false;
        root.reset();
    }

    @Nullable
    public TimingState getMeasurement(String s) {
        return root.getChild(s);
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

        @Nullable
        public <T extends Throwable> Timed measure(ThrowingRunnable<T> runnable) throws T {
            Timed duration;
            start();
            try {
                runnable.run();
            } finally {
                duration = stop();
                if (duration != null) {
                    this.total = this.total.plus(duration);
                }
            }
            return duration;
        }

        @Nullable
        public <T extends Throwable, S> TimedValue<S> measure(ThrowingSupplier<S, T> runnable) throws T {
            Timed duration;
            S result;
            start();
            try {
                result = runnable.get();
            } finally {
                duration = stop();
                if (duration != null) {
                    this.total = this.total.plus(duration);
                }
            }
            if (duration != null) {
                return new TimedValue<>(result, duration);
            }

            return null;
        }

        public void plus(Timed duration) {
            this.total = this.total.plus(duration);
        }

        public Map<String, TimingState> mapChildren() {
            return Collections.unmodifiableMap(children);
        }

        public Collection<TimingState> children() {
            return children.values();
        }

        @NotNull
        public <T> Optional<T> map(@NotNull Function<Timed, T> mapper) {
            return Optional.ofNullable(mapper.apply(total));
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

    public interface Visitor {

        void accept(TimingState state, int depth);

        default void nest() {}

        default void unnest() {}

        default void cleanup() {}

        default void acceptRoot(TimingState state) {}
    }
}
