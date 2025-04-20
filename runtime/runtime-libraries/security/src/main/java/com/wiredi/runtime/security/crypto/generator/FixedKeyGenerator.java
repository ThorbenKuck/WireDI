package com.wiredi.runtime.security.crypto.generator;

public class FixedKeyGenerator implements KeyGenerator {

    private final byte[] key;

    public FixedKeyGenerator(byte[] key) {
        this.key = key;
    }

    @Override
    public int getKeyLength() {
        return key.length;
    }

    @Override
    public byte[] generateKey() {
        return key;
    }
}
