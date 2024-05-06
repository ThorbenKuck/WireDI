package com.wiredi.runtime.async.state;

public class StateException extends RuntimeException {

    public StateException(String message, Throwable cause) {
        super(message, cause);
    }

    public StateException(String message) {
        super(message);
    }
}
