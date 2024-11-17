package com.wiredi.runtime.messaging;

import org.jetbrains.annotations.NotNull;

/**
 * This interface defines algorithms to handle outgoing {@link Message}
 * <p>
 * Most specifically, this interface is invoked right after a payload has been serialized in
 * {@link MessagingEngine#serialize(Object)}.
 * It is not invoked when an ingoing {@link Message}, is constructed.
 * For this please refer to {@link RequestAware#started(Message)} to alter messages that have been received.
 * <p>
 * This interface can be used to alter or introspect a {@link Message} before it is sent out.
 */
public interface MessageInterceptor {

    /**
     * Handle a {@link Message} that is about to be sent out.
     * <p>
     * This method may construct a new Message to replace the original one.
     *
     * @param message the {@link Message} that was just serialized
     * @param <D>     the generic {@link MessageDetails} type
     * @return the message to replace the original input message
     */
    @NotNull
    <D extends MessageDetails> Message<D> postConstruction(@NotNull Message<D> message);
}
