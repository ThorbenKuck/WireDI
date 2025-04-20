package com.wiredi.runtime.security.crypto;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Defines a cryptographic algorithm interface for password encoding and verification.
 * This interface provides methods for encoding passwords, verifying matches, and handling
 * algorithm-specific password formats.
 *
 * <p>Implementations of this interface represent different cryptographic algorithms
 * (e.g., BCrypt, Argon2) and handle the encoding and verification of passwords using
 * their specific implementations.</p>
 *
 * <p>The interface supports a standard format for encoded passwords:
 * {@code algorithmIdentifier:encodedPassword}
 * where the algorithm identifier is optional.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * CryptographicAlgorithm bcrypt = new BCryptAlgorithm();
 *
 * // Encode a password
 * String encoded = bcrypt.encode("myPassword");
 *
 * // Verify a password
 * boolean matches = bcrypt.matches("myPassword", encoded);
 * }</pre>
 */
public interface CryptographicAlgorithm {

    /**
     * Determines the algorithm identifier from an encoded password string.
     * Parses the encoded password string to extract the algorithm identifier if present.
     *
     * <p>Format: {@code algorithmIdentifier:encodedPassword}</p>
     *
     * @param encodedPassword the encoded password string to parse
     * @return the algorithm identifier if present, null if no algorithm identifier is found
     * @throws IllegalArgumentException if the encoded password is null
     */
    @Nullable
    static String determineAlgorithm(@NotNull String encodedPassword) {
        String[] segments = encodedPassword.split(":");
        if (segments.length == 1) {
            return null;
        } else {
            return segments[0];
        }
    }

    /**
     * Encodes the provided raw password using the implemented cryptographic algorithm.
     *
     * @param rawPassword the password to encode
     * @return the encoded password string
     * @throws IllegalArgumentException if the raw password is null
     */
    @NotNull
    String encode(@NotNull CharSequence rawPassword);

    /**
     * Returns the unique identifier for this cryptographic algorithm.
     * <p>
     * This identifier is used in the encoded password format: {@code identifier:encodedPassword}
     *
     * @return the algorithm identifier string
     */
    @NotNull
    String identifier();

    /**
     * Verifies if the provided raw password matches the encoded password.
     *
     * @param rawPassword the raw password to check
     * @param encodedPassword the encoded password to check against
     * @return true if the passwords match, false otherwise
     * @throws IllegalArgumentException if either parameter is null or invalid
     */
    boolean matches(@NotNull CharSequence rawPassword, @NotNull String encodedPassword);

    /**
     * Removes the algorithm identifier prefix from an encoded password if present.
     * This method is used to extract the actual encoded password value without the algorithm identifier.
     *
     * @param encodedPassword the encoded password string to sanitize
     * @return the encoded password without the algorithm identifier prefix
     * @throws IllegalArgumentException if the encoded password is null
     */
    @NotNull
    default String sanitizeInput(@NotNull String encodedPassword) {
        @Nullable String algorithm = determineAlgorithm(encodedPassword);
        if (algorithm == null) {
            return encodedPassword;
        } else {
            return encodedPassword.replaceFirst(algorithm + ":", "");
        }
    }

    /**
     * Adds the algorithm identifier to a password string.
     * Creates a properly formatted password string including the algorithm identifier.
     *
     * @param rawPassword the password string to identify
     * @return the password string with the algorithm identifier prefix
     * @throws IllegalArgumentException if the raw password is null
     */
    @NotNull
    default String identify(@NotNull String rawPassword) {
        return identifier() + ":" + rawPassword;
    }
}
