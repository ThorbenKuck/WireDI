package com.wiredi.runtime.security.crypto;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry and facade for cryptographic algorithms used in security operations.
 * <p>
 * The {@code Algorithms} class manages a collection of {@link CryptographicAlgorithm} implementations
 * and provides a unified interface for cryptographic operations like password encoding and verification.
 * It maintains a registry of algorithms by their identifiers and designates one algorithm as the system
 * default for operations where no specific algorithm is requested.
 * <p>
 * Each algorithm is registered with a unique identifier that is used both for lookup in the registry
 * and as a prefix in encoded password strings to indicate which algorithm was used for encoding.
 * <p>
 * Example usage:
 * <pre>{@code
 * // Create algorithms registry with BCrypt as the system default
 * BCryptAlgorithm bcrypt = new BCryptAlgorithm();
 * Argon2Algorithm argon2 = new Argon2Algorithm();
 * Algorithms algorithms = new Algorithms(List.of(bcrypt, argon2), bcrypt);
 *
 * // Encode a password using the system default algorithm
 * String encodedDefault = algorithms.encode("myPassword");
 *
 * // Encode a password using a specific algorithm
 * String encodedArgon2 = algorithms.encode("myPassword", "argon2");
 *
 * // Verify a password against an encoded value
 * // (the algorithm is automatically determined from the encoded string)
 * boolean isValid = algorithms.matches("myPassword", encodedArgon2);
 * }</pre>
 *
 * <p>The class uses {@link ConcurrentHashMap} for thread-safe access to the algorithm registry,
 * making it suitable for concurrent use in multi-threaded applications.
 *
 * @see CryptographicAlgorithm
 * @see NoOpAlgorithm
 */
public class Algorithms {

    private final Map<String, CryptographicAlgorithm> algorithms = new ConcurrentHashMap<>();
    private final CryptographicAlgorithm systemAlgorithm;

    public Algorithms(List<CryptographicAlgorithm> algorithms, CryptographicAlgorithm systemAlgorithm) {
        this.systemAlgorithm = systemAlgorithm;
        algorithms.forEach(a -> {
            if (this.algorithms.containsKey(a.identifier())) {
                throw new IllegalArgumentException("Multiple algorithms with the same identifier registered: " + a.identifier());
            } else {
                this.algorithms.put(a.identifier(), a);
            }
        });
    }

    public Algorithms(List<CryptographicAlgorithm> algorithms) {
        this(algorithms, new NoOpAlgorithm());
    }

    public Algorithms() {
        this(Collections.emptyList(), new NoOpAlgorithm());
    }

    /**
     * Adds a cryptographic algorithm to the registry.
     * <p>
     * If an algorithm with the same identifier already exists, it will be replaced.
     *
     * @param algorithm the algorithm to add
     */
    public void addAlgorithm(CryptographicAlgorithm algorithm) {
        algorithms.put(algorithm.identifier(), algorithm);
    }

    /**
     * Removes a cryptographic algorithm from the registry.
     * <p>
     * If no algorithm with the specified identifier exists, this method has no effect.
     *
     * @param algorithm the algorithm to remove
     */
    public void removeAlgorithm(CryptographicAlgorithm algorithm) {
        algorithms.get(algorithm.identifier());
    }

    /**
     * Encodes a password using the system default algorithm.
     *
     * @param rawPassword the password to encode
     * @return the encoded password string, prefixed with the algorithm identifier
     */
    public String encode(CharSequence rawPassword) {
        return systemAlgorithm.encode(rawPassword);
    }

    /**
     * Encodes a password using the specified algorithm.
     *
     * @param rawPassword the password to encode
     * @param algorithm the identifier of the algorithm to use
     * @return the encoded password string, prefixed with the algorithm identifier
     * @throws IllegalArgumentException if no algorithm with the specified identifier is registered
     */
    public String encode(CharSequence rawPassword, String algorithm) {
        CryptographicAlgorithm cryptographicAlgorithm = this.algorithms.get(algorithm);
        if (cryptographicAlgorithm == null) {
            throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }

        return cryptographicAlgorithm.encode(rawPassword);
    }

    /**
     * Verifies that a raw password matches an encoded password.
     * <p>
     * This method extracts the algorithm identifier from the encoded password string,
     * looks up the corresponding algorithm, and delegates to that algorithm's
     * {@link CryptographicAlgorithm#matches(CharSequence, String)} method.
     *
     * @param rawPassword the raw password to verify
     * @param encodedPassword the encoded password to check against, prefixed with an algorithm identifier
     * @return true if the passwords match, false otherwise
     * @throws IllegalArgumentException if the encoded password format is invalid or if the
     *         algorithm identifier isn't recognized
     */
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        String algorithm = CryptographicAlgorithm.determineAlgorithm(encodedPassword);
        if (algorithm == null) {
            throw new IllegalArgumentException("Incorrectly encoded password. Expected algorithm prefix, separated by :");
        }

        CryptographicAlgorithm cryptographicAlgorithm = algorithms.get(algorithm);
        if (cryptographicAlgorithm == null) {
            throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }

        return cryptographicAlgorithm.matches(rawPassword, encodedPassword);
    }
}
