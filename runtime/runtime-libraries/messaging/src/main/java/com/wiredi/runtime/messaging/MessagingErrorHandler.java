package com.wiredi.runtime.messaging;

import com.wiredi.runtime.messaging.errors.MessagingException;

/**
 * An interface on how to handle errors during MessageProcessing.
 * <p>
 * This error handler is used by the {@link RequestContext} to handle "expected" errors.
 * These include errors while processing a {@link Message}, or errors that are raised by a {@link RequestAware} instance,
 * apart from errors of {@link RequestAware#completed(Message)}.
 * <p>
 * The error handler may decide if the error is returned silently (by returning a {@link MessagingResult} instance),
 * or by throwing an Exception.
 * When an exception is raised, using classes (like the {@link MessagingEngine}) may decide on how to handle the exception.
 * It is generally recommended to return a {@link MessagingResult}.
 */
public interface MessagingErrorHandler {

    /**
     * A simple default instance which returns a {@link MessagingResult.Failed} instance of the throwable
     */
    MessagingErrorHandler DEFAULT = (message, throwable) -> new MessagingResult.Failed(throwable);
    /**
     * A simple instance that rethrows the error.
     */
    MessagingErrorHandler RETHROW = (message, throwable) -> {
        if (throwable instanceof RuntimeException r) {
            throw r;
        } else {
            throw new MessagingException(throwable);
        }
    };

    /**
     * Handles the provided {@code throwable}.
     *
     * @param message   the message which caused the error
     * @param throwable the error that occurred
     * @return a MessagingResult on how to proceed after the throwable
     */
    MessagingResult handleError(Message<?> message, Throwable throwable);
}
