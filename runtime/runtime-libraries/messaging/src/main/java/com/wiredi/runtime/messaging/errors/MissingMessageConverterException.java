package com.wiredi.runtime.messaging.errors;

import com.wiredi.runtime.messaging.Message;

public class MissingMessageConverterException extends MessageException {

    private final Message<?, ?> converter;

    public MissingMessageConverterException(Message<?, ?> message) {
        super(message, "No message converter found for " + message);
        this.converter = message;
    }

    public Message<?, ?> getConverter() {
        return converter;
    }
}
