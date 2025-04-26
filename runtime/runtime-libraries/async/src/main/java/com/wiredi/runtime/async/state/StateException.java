package com.wiredi.runtime.async.state;

/**
 * Exception thrown when there's an error related to state operations.
 * <p>
 * This exception is typically thrown in the following scenarios:
 * <ul>
 *   <li>When trying to access a state that isn't set</li>
 *   <li>When trying to access a state that is marked as dirty (has an error)</li>
 *   <li>When a state operation fails for any other reason</li>
 * </ul>
 * <p>
 * As a RuntimeException, this exception doesn't need to be declared in method signatures
 * or caught explicitly.
 *
 * @see State
 * @see AbstractState
 */
public class StateException extends RuntimeException {

    /**
     * Constructs a new StateException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public StateException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new StateException with the specified detail message.
     *
     * @param message the detail message
     */
    public StateException(String message) {
        super(message);
    }
}
