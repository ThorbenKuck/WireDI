package com.wiredi.runtime.messaging.errors;

import com.wiredi.runtime.messaging.Message;

public class MissingMessageDetailsException extends MessagingException {

    private final Message<?> message;

    public MissingMessageDetailsException(Message<?> message) {
        this(message, "Missing details in message " + message);
    }

    public MissingMessageDetailsException(Message<?> message, String errorMessage) {
        super(errorMessage);
        this.message = message;
    }

    public Message<?> getRootMessage() {
        return message;
    }
}
