package com.wiredi.runtime.messaging;

/**
 * This interface is used to determine if any given {@link Message} should be handled or skipped.
 * <p>
 * This interface is used by the {@link RequestContext}.
 *
 * @see RequestContext
 * @see MessagingEngine
 */
public interface MessageFilter {

    /**
     * Determines if the provided {@code message} should be skipped.
     * <p>
     * If true is returned, the {@link Message} will not be processed, but {@link MessagingResult.SkipMessage} will be
     * returned instead.
     *
     * @param message the message to check
     * @return true, if the Message should not be handled. false if the message SHOULD be handled.
     */
    boolean shouldSkip(Message<?> message);

}
