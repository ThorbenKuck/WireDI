package com.wiredi.runtime.messaging.errors;

import com.wiredi.runtime.messaging.Message;

public class MessageException extends RuntimeException {

    private final Message<?, ?> message;

    public MessageException(Message<?, ?> message, String errorMessage) {
        super(errorMessage);
        this.message = message;
    }
}
