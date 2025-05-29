package com.wiredi.compiler.errors;

import java.util.List;

public class CompositeProcessingException extends RuntimeException {

    private final List<ProcessingException> exceptions;

    public CompositeProcessingException(List<ProcessingException> exceptions) {
        this.exceptions = exceptions;
    }

    public List<ProcessingException> getExceptions() {
        return exceptions;
    }
}
