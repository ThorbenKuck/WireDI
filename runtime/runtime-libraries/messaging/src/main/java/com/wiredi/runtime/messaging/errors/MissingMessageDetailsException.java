package com.wiredi.runtime.messaging.errors;

import com.wiredi.runtime.messaging.Message;

public class MissingMessageDetailsException extends MessageException {
    public MissingMessageDetailsException(Message<?, ?> message) {
        super(message, "Missing details in message " + message);
    }
    public MissingMessageDetailsException(Message<?, ?> message, String errorMessage) {
        super(message, errorMessage);
    }
}
