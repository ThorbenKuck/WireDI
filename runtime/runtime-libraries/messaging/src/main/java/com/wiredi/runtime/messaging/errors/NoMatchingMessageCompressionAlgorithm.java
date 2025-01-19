package com.wiredi.runtime.messaging.errors;

import com.wiredi.runtime.messaging.compression.Algorithm;

import java.util.List;

public class NoMatchingMessageCompressionAlgorithm extends MessagingException {

    private final List<Algorithm> algorithms;

    public NoMatchingMessageCompressionAlgorithm(List<Algorithm> algorithms) {
        this.algorithms = algorithms;
    }

    public List<Algorithm> getAlgorithms() {
        return algorithms;
    }
}
