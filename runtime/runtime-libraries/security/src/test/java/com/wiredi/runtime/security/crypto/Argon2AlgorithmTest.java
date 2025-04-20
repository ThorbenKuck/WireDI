package com.wiredi.runtime.security.crypto;

import com.wiredi.runtime.security.crypto.generator.KeyGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Argon2AlgorithmTest {

    @Test
    public void sameSaltResultsInDifferentHashes() {
        // Arrange
        String password = "password";
        byte[] salt = "saltsaltsaltsalt".getBytes();

        // Act
        String encodedOne = new Argon2Algorithm(KeyGenerator.just(salt)).encode(password);
        String encodedTwo = new Argon2Algorithm(KeyGenerator.just(salt)).encode(password);

        // Assert
        assertNotEquals(encodedOne, encodedTwo);
    }

    @Test
    public void differentSaltResultsInDifferentHashes() {
        // Arrange
        String password = "password";

        // Act
        String encodedOne = new Argon2Algorithm(KeyGenerator.just("123456789012345a".getBytes())).encode(password);
        String encodedTwo = new Argon2Algorithm(KeyGenerator.just("123456789012345b".getBytes())).encode(password);

        // Assert
        assertNotEquals(encodedOne, encodedTwo);
    }
}