package com.wiredi.runtime.security.crypto;

import com.wiredi.logging.Logging;
import org.jetbrains.annotations.NotNull;

/**
 * A no-operation implementation of {@link CryptographicAlgorithm} that provides no actual security.
 *
 * <p><strong>WARNING: FOR DEVELOPMENT ONLY!</strong></p>
 *
 * <p>This algorithm is <em>fundamentally insecure</em> and stores passwords in plain text with only
 * a simple identifier prefix. It must <strong>never</strong> be used in production environments or
 * for any security-sensitive application.</p>
 *
 * <p>Key security issues:</p>
 * <ul>
 *   <li>Passwords are stored essentially in plain text (only prefixed with an identifier)</li>
 *   <li>No cryptographic protection is applied to the password</li>
 *   <li>Passwords can be easily extracted from storage</li>
 *   <li>Vulnerable to all types of password attacks</li>
 * </ul>
 *
 * <p>Appropriate use cases:</p>
 * <ul>
 *   <li>Local development environments with non-sensitive data</li>
 *   <li>Testing authentication flows without security requirements</li>
 *   <li>Demonstration purposes in controlled environments</li>
 *   <li>Educational contexts to demonstrate why proper password hashing is necessary</li>
 * </ul>
 *
 * <p>When instantiated, this class emits warning logs to help prevent accidental use in production.</p>
 *
 * <p>For production use, always replace with a secure implementation like {@link BCryptAlgorithm}
 * or {@link Argon2Algorithm}.</p>
 *
 * @see CryptographicAlgorithm
 * @see BCryptAlgorithm
 * @see Argon2Algorithm
 */
public class NoOpAlgorithm implements CryptographicAlgorithm {

    /**
     * Logger instance for emitting warnings about this insecure implementation.
     */
    private static final Logging logger = Logging.getInstance(NoOpAlgorithm.class);

    /**
     * Creates a new NoOpAlgorithm instance.
     *
     * <p>Logs a warning message to emphasize that this implementation is insecure
     * and should not be used in production environments.</p>
     */
    public NoOpAlgorithm() {
        logger.warn(() -> "NoOpCryptographicAlgorithm instantiated. This is insecure. Please consider to use a different implementation of CryptographicAlgorithm.");
    }

    /**
     * "Encodes" the password by simply returning it prefixed with the algorithm identifier.
     *
     * <p><strong>WARNING:</strong> This method provides NO actual security and should NOT be used with real passwords
     * in production environments.</p>
     *
     * @param rawPassword the password to "encode"
     * @return the raw password with the "noop:" prefix
     */
    @Override
    public @NotNull String encode(@NotNull CharSequence rawPassword) {
        return identify(rawPassword.toString());
    }

    /**
     * Returns the identifier for this algorithm: "noop" (no operation).
     *
     * @return the string "noop"
     */
    @Override
    public @NotNull String identifier() {
        return "noop";
    }

    /**
     * Verifies a password by simple string equality comparison.
     *
     * <p><strong>WARNING:</strong> This provides no protection against timing attacks or any
     * other security measures found in proper cryptographic implementations.</p>
     *
     * @param rawPassword the raw password to verify
     * @param encodedPassword the "encoded" password to check against
     * @return true if the raw password matches the encoded password (after removing the prefix)
     */
    @Override
    public boolean matches(@NotNull CharSequence rawPassword, @NotNull String encodedPassword) {
        return rawPassword.equals(sanitizeInput(encodedPassword));
    }
}
