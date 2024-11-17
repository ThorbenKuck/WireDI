package com.wiredi.runtime.messaging.errors;

public class MessagingException extends RuntimeException {

    public MessagingException() {
        super();
    }

    public MessagingException(String message) {
        super(message);
    }

    public MessagingException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessagingException(Throwable cause) {
        super(cause);
    }
}
