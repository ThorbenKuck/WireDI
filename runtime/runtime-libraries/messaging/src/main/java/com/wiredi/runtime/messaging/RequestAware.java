package com.wiredi.runtime.messaging;

import com.wiredi.runtime.domain.Ordered;

/**
 * An interface to listen to specific lifecycle events while processing messages.
 * <p>
 * Implementations can use thread local instances, as all methods will be invoked on the same thread.
 */
public interface RequestAware extends Ordered {

    /**
     * Invoked before the provided {@code message} is processed.
     *
     * @param message The message that is being processed
     * @return the message to use
     */
    default Message<?> started(Message<?> message) {
        return message;
    }

    /**
     * Invoked if the {@code message} was processed successfully.
     *
     * @param message The message that is being processed
     */
    default void successful(Message<?> message) {
    }

    /**
     * Invoked if there was an error while the {@code message} was processed.
     * <p>
     * Rethrowing the exception will not be necessary, as the {@link RequestContext} will throw the exception.
     * <p>
     * Errors include any error that occurred during the message processing, as well as any error that was raised
     * in {@link #started(Message)} or {@link #successful(Message)}
     *
     * @param message   The message that is being processed
     * @param throwable The error that occurred
     */
    default void failed(Message<?> message, Throwable throwable) {
    }

    /**
     * Invoked after the processing is complete.
     * <p>
     * This method is invoked after either {@link #successful(Message)} or {@link #failed(Message, Throwable)} are invoked.
     *
     * @param message The message that is being processed
     */
    default void completed(Message<?> message) {
    }
}
