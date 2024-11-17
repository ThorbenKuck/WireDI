package com.wiredi.integration.jackson.exceptions;

public class SerializationFailedException extends RuntimeException {

    private final Object payload;

    public SerializationFailedException(Object payload, Throwable cause) {
        super("Failed to serialize " + payload, cause);
        this.payload = payload;
    }

    public Object getPayload() {
        return payload;
    }
}
