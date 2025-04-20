package com.wiredi.runtime.security.crypto.generator;

import java.security.SecureRandom;
import java.util.Random;

public class RandomKeyGenerator implements KeyGenerator {

    private final Random random;
    private final int keyLength;
    private static final int DEFAULT_KEY_LENGTH = 16;

    public RandomKeyGenerator() {
        this(new SecureRandom(), DEFAULT_KEY_LENGTH);
    }

    public RandomKeyGenerator(Random random) {
        this(random, DEFAULT_KEY_LENGTH);
    }

    public RandomKeyGenerator(int keyLength) {
        this(new SecureRandom(), keyLength);
    }

    public RandomKeyGenerator(Random random, int keyLength) {
        this.random = random;
        this.keyLength = keyLength;
    }

    @Override
    public int getKeyLength() {
        return keyLength;
    }

    @Override
    public byte[] generateKey() {
        byte[] bytes = new byte[getKeyLength()];
        this.random.nextBytes(bytes);
        return bytes;
    }
}
