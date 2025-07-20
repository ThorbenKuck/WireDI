package com.wiredi.runtime.values;

/**
 * Represents the result of a {@link Value#ifPresent(java.util.function.Consumer)} operation.
 * <p>
 * This interface is part of the fluent API for handling the presence or absence of a value.
 * After checking if a value is present using {@link Value#ifPresent(java.util.function.Consumer)},
 * this interface allows specifying an action to take if the value was missing.
 * <p>
 * This is a sealed interface with two implementations:
 * <ul>
 *   <li>{@link ValuePresentIfPresentStage} - Returned when the value was present</li>
 *   <li>{@link ValueMissingIfPresentStage} - Returned when the value was missing</li>
 * </ul>
 *
 * @see Value#ifPresent(java.util.function.Consumer)
 */
public sealed interface IfPresentStage {
    /**
     * Creates an IfPresentStage instance indicating that a value was present.
     * <p>
     * This factory method is used internally by Value implementations to create
     * an IfPresentStage when a value is present.
     *
     * @return An IfPresentStage instance for a present value
     */
    static IfPresentStage wasPresent() {
        return ValuePresentIfPresentStage.INSTANCE;
    }

    /**
     * Creates an IfPresentStage instance indicating that a value was missing.
     * <p>
     * This factory method is used internally by Value implementations to create
     * an IfPresentStage when a value is missing.
     *
     * @return An IfPresentStage instance for a missing value
     */
    static IfPresentStage wasMissing() {
        return ValueMissingIfPresentStage.INSTANCE;
    }

    /**
     * Specifies an action to take if the value was missing.
     * <p>
     * If the value was present when {@link Value#ifPresent(java.util.function.Consumer)}
     * was called, this method does nothing. If the value was missing, the provided
     * runnable is executed.
     *
     * @param runnable The action to take if the value was missing
     */
    void orElse(Runnable runnable);

    /**
     * Implementation of IfPresentStage for when a value is missing.
     * <p>
     * This implementation executes the runnable provided to {@link #orElse(Runnable)}
     * since the value was missing.
     */
    final class ValueMissingIfPresentStage implements IfPresentStage {

        private static final IfPresentStage INSTANCE = new ValueMissingIfPresentStage();

        @Override
        public void orElse(Runnable runnable) {
            runnable.run();
        }
    }

    /**
     * Implementation of IfPresentStage for when a value is present.
     * <p>
     * This implementation ignores the runnable provided to {@link #orElse(Runnable)}
     * since the value was present and the consumer provided to
     * {@link Value#ifPresent(java.util.function.Consumer)} has already been executed.
     */
    final class ValuePresentIfPresentStage implements IfPresentStage {

        private static final IfPresentStage INSTANCE = new ValuePresentIfPresentStage();

        @Override
        public void orElse(Runnable runnable) {
            // Ignore
        }
    }
}
