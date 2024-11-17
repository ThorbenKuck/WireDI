package com.wiredi.runtime.messaging.errors;

import com.wiredi.runtime.messaging.Message;

public class MissingMessageConverterException extends MessagingException {

    private final Message<?> message;

    public MissingMessageConverterException(Message<?> message) {
        super("No message converter found for " + message);
        this.message = message;
    }

    public Message<?> getRootMessage() {
        return message;
    }
}
