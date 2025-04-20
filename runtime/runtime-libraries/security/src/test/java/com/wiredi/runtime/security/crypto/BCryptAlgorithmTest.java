package com.wiredi.runtime.security.crypto;

import com.wiredi.runtime.security.crypto.generator.KeyGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BCryptAlgorithmTest {

    @Test
    public void sameSaltResultsInTheSameHash() {
        // Arrange
        String password = "password";
        String encodedPassword = "bcrypt:$2a$12$a0DqbFLfZFPxWUvya0Dqb.BTcwtc1896E7QSjA/13EYCoVrnolws.";
        byte[] salt = "saltsaltsaltsalt".getBytes();

        // Act
        String encodedOne = new BCryptAlgorithm(KeyGenerator.just(salt)).encode(password);
        String encodedTwo = new BCryptAlgorithm(KeyGenerator.just(salt)).encode(password);

        // Assert
        assertEquals(encodedOne, encodedTwo);
        assertEquals(encodedPassword, encodedOne);
    }

    @Test
    public void differentSaltResultsInDifferentHash() {
        // Arrange
        String password = "password";

        // Act
        String encodedOne = new BCryptAlgorithm(KeyGenerator.just("123456789012345a".getBytes())).encode(password);
        String encodedTwo = new BCryptAlgorithm(KeyGenerator.just("123456789012345b".getBytes())).encode(password);

        // Assert
        assertNotEquals(encodedOne, encodedTwo);
    }
}
