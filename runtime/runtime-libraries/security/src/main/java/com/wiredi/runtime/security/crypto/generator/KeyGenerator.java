package com.wiredi.runtime.security.crypto.generator;

/**
 * An interface to generate a random key.
 * <p>
 * Implementations can decide how to generate keys.
 * Most notably, a random key can be generated using the {@link RandomKeyGenerator}.
 * <p>
 * For tests, a {@link FixedKeyGenerator} can be used to bypass randomness.
 */
public interface KeyGenerator {

    static KeyGenerator random(int keyLength) {
        return new RandomKeyGenerator(keyLength);
    }

    static KeyGenerator random() {
        return new RandomKeyGenerator();
    }

    static KeyGenerator just(byte[] key) {
        return new FixedKeyGenerator(key);
    }

    /**
     * The length of the key this generator generates.
     *
     * @return the key length.
     */
    int getKeyLength();

    /**
     * Generate a new key.
     *
     * @return a new key.
     */
    byte[] generateKey();

}
