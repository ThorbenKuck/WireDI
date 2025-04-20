package com.wiredi.runtime.security.crypto;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CryptographicAlgorithmTest {

    @ParameterizedTest
    @MethodSource("algorithms")
    public void happyPathTests(CryptographicAlgorithm algorithm) {
        // Arrange
        Algorithms algorithms = new Algorithms(List.of(algorithm), new NoOpAlgorithm());
        String password = "ThisIsAVerySecurePassw0rd";

        // Act
        String encoded = algorithm.encode(password);

        // Assert
        assertTrue(algorithm.matches(password, encoded));
        assertTrue(algorithms.matches(password, encoded));
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    public void unhappyPathTests(CryptographicAlgorithm algorithm) {
        // Arrange
        Algorithms algorithms = new Algorithms(List.of(algorithm), new NoOpAlgorithm());
        String password = "ThisIsAVerySecurePassw0rd";

        // Act
        String encoded = algorithm.encode(password);

        // Assert
        assertFalse(algorithm.matches("not-" + password, encoded));
        assertTrue(algorithms.matches(password, encoded));
    }

    public static List<Arguments> algorithms() {
        return List.of(
                Arguments.of(new Argon2Algorithm()),
                Arguments.of(new BCryptAlgorithm()),
                Arguments.of(new NoOpAlgorithm())
        );
    }
}