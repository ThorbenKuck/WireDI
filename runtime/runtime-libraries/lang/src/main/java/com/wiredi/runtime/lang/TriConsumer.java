package com.wiredi.runtime.lang;

import java.util.Objects;

public interface TriConsumer<S, T, U> {
    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     */
    void accept(S s, T t, U u);

    /**
     * Returns a composed {@code BiConsumer} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing this operation throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code BiConsumer} that performs in sequence this
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default TriConsumer<S, T, U> andThen(TriConsumer<? super S, ? super T, ? super U> after) {
        Objects.requireNonNull(after);

        return (s, t, u) -> {
            accept(s, t, u);
            after.accept(s, t, u);
        };
    }
}
