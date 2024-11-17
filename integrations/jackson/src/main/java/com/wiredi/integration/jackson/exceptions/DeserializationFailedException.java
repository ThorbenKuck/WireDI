package com.wiredi.integration.jackson.exceptions;

import com.wiredi.runtime.messaging.Message;

public class DeserializationFailedException extends RuntimeException {

    private final Message<?> message;
    private final Class<?> targetType;

    public DeserializationFailedException(Message<?> message, Class<?> targetType, Throwable cause) {
        super("Failed to deserialize " + message + " to type " + targetType, cause);
        this.message = message;
        this.targetType = targetType;
    }

    public Message<?> getOriginalMessage() {
        return message;
    }

    public Class<?> getTargetType() {
        return targetType;
    }
}
