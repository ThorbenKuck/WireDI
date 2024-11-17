package com.wiredi.runtime.messaging.errors;

public class MissingMessageSerializerException extends MessagingException {

    private final Object payload;

    public MissingMessageSerializerException(Object payload) {
        super("No MessageConverter could serialize the payload " + payload);
        this.payload = payload;
    }

    public Object getPayload() {
        return payload;
    }
}
