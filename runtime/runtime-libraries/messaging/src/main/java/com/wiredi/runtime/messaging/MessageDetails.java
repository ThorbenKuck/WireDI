package com.wiredi.runtime.messaging;

/**
 * This is a marker interface for integrations to enhance MessageConversions.
 * <p>
 * Different integrations of the message conversion can provide their own MessageDetails.
 * {@link MessageConverter} instances can specify to only check for certain implementations or specifications of this
 * interface, or use these details to change the conversion.
 */
public interface MessageDetails {

    /**
     * An empty implementation that is used instead of null.
     */
    MessageDetails NONE = new MessageDetails() {};

    default boolean isNotNone() {
        return this != NONE;
    }

    default boolean isNone() {
        return this == NONE;
    }
}
