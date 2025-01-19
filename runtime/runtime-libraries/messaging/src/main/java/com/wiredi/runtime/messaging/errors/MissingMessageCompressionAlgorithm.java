package com.wiredi.runtime.messaging.errors;

import com.wiredi.runtime.messaging.compression.Algorithm;

public class MissingMessageCompressionAlgorithm extends MessagingException {

    private final Algorithm algorithm;

    public MissingMessageCompressionAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }
}
