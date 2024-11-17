package com.wiredi.runtime.messaging;

import com.wiredi.runtime.messaging.errors.MessagingException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An interface on the result of processing {@link Message}.
 * <p>
 * Implementations may decide on how (and if) errors are rethrown.
 * Developers can decide to use the default implementations ({@link Success}, {@link Failed} or {@link SkipMessage}) to
 * tell the {@link MessagingEngine} how to continue.
 * Alternatively, developers may provide custom implementations that behave differently.
 */
public interface MessagingResult {

    default boolean wasSuccessful() {
        return false;
    }

    default boolean wasSkipped() {
        return false;
    }

    default boolean hasFailed() {
        return false;
    }

    default Throwable errorOr(Supplier<Throwable> other) {
        Throwable error = error();
        if (error != null) {
            return error;
        }
        return other.get();
    }

    @Nullable
    default Throwable error() {
        return null;
    }

    @Nullable
    default Object result() {
        return null;
    }

    @Nullable
    default <T> T getResultAs() {
        return (T) result();
    }

    default void tryPropagateError(Function<Throwable, Throwable> converter) {
    }

    default void tryPropagateError() {
        tryPropagateError(Function.identity());
    }

    interface Faulty extends MessagingResult {
        @Override
        default Throwable errorOr(Supplier<Throwable> other) {
            return error();
        }

        @Override
        default boolean hasFailed() {
            return true;
        }
    }

    record Success(
            @Nullable Object result
    ) implements MessagingResult {

        @NotNull
        public <T> T getResultAs() {
            return (T) result;
        }

        @Override
        public boolean wasSuccessful() {
            return true;
        }

        @Override
        public int hashCode() {
            return Success.class.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            return obj.getClass() == Success.class;
        }
    }

    class SkipMessage implements MessagingResult {
        @Override
        public boolean wasSkipped() {
            return true;
        }

        @Override
        public int hashCode() {
            return Success.class.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            return obj.getClass() == SkipMessage.class;
        }
    }

    record Failed(
            @NotNull Throwable error
    ) implements Faulty {
        @Override
        public void tryPropagateError(Function<Throwable, Throwable> converter) {
            raise(converter.apply(error));
        }

        @Override
        public Throwable error() {
            return error;
        }

        private void raise(Throwable throwable) {
            if (throwable instanceof RuntimeException r) {
                throw r;
            } else {
                throw new MessagingException(throwable);
            }
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof Failed failed)) return false;
            return Objects.equals(error, failed.error);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(error);
        }
    }
}
