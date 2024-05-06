package com.wiredi.integration.jackson.exceptions;

import com.wiredi.runtime.messaging.Message;

public class SerializationFailedException extends RuntimeException {

    private final Message<?, ?> message;

    public SerializationFailedException(Message<?, ?> message, Throwable cause) {
        super("Failed to serialize " + message, cause);
        this.message = message;
    }

    public Message<?, ?> getOriginalMessage() {
        return message;
    }
}
