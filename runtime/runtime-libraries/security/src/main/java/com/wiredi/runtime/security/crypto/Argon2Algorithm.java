package com.wiredi.runtime.security.crypto;

import com.wiredi.runtime.security.crypto.generator.KeyGenerator;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.util.Arrays;
import org.jetbrains.annotations.NotNull;

import java.util.Base64;

/**
 * Implementation of the {@link CryptographicAlgorithm} interface using the Argon2 password hashing algorithm.
 * 
 * <p>Argon2 is a key derivation function designed to be resistant against GPU cracking attacks and
 * side-channel attacks. It was selected as the winner of the Password Hashing Competition in 2015 and
 * is recommended for password hashing in modern applications.</p>
 * 
 * <p>This implementation uses the Argon2id variant by default, which combines the benefits of
 * Argon2i (resistance against side-channel attacks) and Argon2d (resistance against GPU attacks).</p>
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Configurable memory, CPU cost, parallelism, and hash output length</li>
 *   <li>Secure salt generation with customizable key generator</li>
 *   <li>Support for all Argon2 variants (Argon2d, Argon2i, Argon2id)</li>
 *   <li>Standard encoding format compatible with other implementations</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * // Use default parameters (suitable for most applications)
 * CryptographicAlgorithm argon2 = new Argon2Algorithm();
 * 
 * // Encode a password
 * String encoded = argon2.encode("mySecretPassword");
 * 
 * // Verify a password
 * boolean matches = argon2.matches("mySecretPassword", encoded);
 * 
 * // Custom parameters for higher security
 * Properties secureProps = new Properties(
 *     64,    // 64-byte hash output
 *     4,     // 4 parallel threads
 *     1 << 16, // 64MB memory cost (2^16 KB)
 *     3      // 3 iterations
 * );
 * CryptographicAlgorithm secureArgon2 = new Argon2Algorithm(secureProps);
 * }</pre>
 * 
 * @see CryptographicAlgorithm
 * @see KeyGenerator
 * @see <a href="https://github.com/P-H-C/phc-winner-argon2">Argon2 reference implementation</a>
 */
public class Argon2Algorithm implements CryptographicAlgorithm {

    /**
     * Default properties for Argon2 with reasonably secure parameters.
     * <ul>
     *   <li>32 bytes (256 bits) hash output length</li>
     *   <li>1 iteration (time cost)</li>
     *   <li>16MB memory cost (2^14 KB)</li>
     *   <li>2 parallel threads</li>
     * </ul>
     * These parameters balance security and performance for general use cases.
     */
    private static final Properties DEFAULT_PROPERTIES = new Properties(32, 1, 1 << 14, 2);
    
    /**
     * Base64 encoder without padding for compact string representation of hashes.
     */
    private static final Base64.Encoder b64encoder = Base64.getEncoder().withoutPadding();
    
    /**
     * Base64 decoder for parsing encoded passwords.
     */
    private static final Base64.Decoder b64decoder = Base64.getDecoder();
    
    /**
     * Configuration properties for the Argon2 algorithm.
     */
    private final Properties properties;
    
    /**
     * Key generator for creating random salt values.
     */
    private final KeyGenerator keyGenerator;

    /**
     * Creates an Argon2Algorithm with default properties and a secure random key generator.
     * Suitable for most production use cases.
     */
    public Argon2Algorithm() {
        this(DEFAULT_PROPERTIES, KeyGenerator.random());
    }

    /**
     * Creates an Argon2Algorithm with custom properties and a secure random key generator.
     * Use this constructor when you need to adjust the algorithm parameters for specific
     * security requirements or performance constraints.
     *
     * @param properties the custom Argon2 configuration properties
     */
    public Argon2Algorithm(Properties properties) {
        this(properties, KeyGenerator.random());
    }

    /**
     * Creates an Argon2Algorithm with default properties and a custom key generator.
     * This constructor is useful for testing or when you need to control the salt generation.
     *
     * @param keyGenerator the key generator to use for salt creation
     */
    public Argon2Algorithm(KeyGenerator keyGenerator) {
        this(DEFAULT_PROPERTIES, keyGenerator);
    }

    /**
     * Creates an Argon2Algorithm with custom properties and a custom key generator.
     * Provides full control over the algorithm's configuration.
     *
     * @param properties the custom Argon2 configuration properties
     * @param randomKeyGenerator the key generator to use for salt creation
     */
    public Argon2Algorithm(Properties properties, @NotNull KeyGenerator randomKeyGenerator) {
        this.properties = properties;
        this.keyGenerator = randomKeyGenerator;
    }

    /**
     * Encodes the provided raw password using the Argon2 algorithm.
     * Generates a random salt using the configured key generator and applies
     * the Argon2 algorithm with the configured parameters.
     *
     * @param rawPassword the password to encode
     * @return the Argon2-encoded password string with algorithm identifier prefix
     * @throws IllegalArgumentException if the raw password is null or invalid
     */
    @Override
    public @NotNull String encode(@NotNull CharSequence rawPassword) {
        byte[] hash = this.keyGenerator.generateKey();
        Argon2Parameters parameters = new Argon2Parameters
                .Builder(Argon2Parameters.ARGON2_id)
                .withSalt(hash)
                .withParallelism(this.properties.parallelism)
                .withMemoryAsKB(this.properties.memory)
                .withIterations(this.properties.iterations)
                .build();

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(parameters);
        generator.generateBytes(rawPassword.toString().toCharArray(), hash);
        return identify(doEncode(hash, parameters));
    }

    /**
     * Returns the identifier for this algorithm: "argon2".
     * This identifier is used in the encoded password format.
     *
     * @return the string "argon2"
     */
    @Override
    public @NotNull String identifier() {
        return "argon2";
    }

    /**
     * Verifies that the provided raw password matches the encoded password.
     * Decodes the encoded password string, extracts the Argon2 parameters,
     * and verifies the password using a constant-time comparison.
     *
     * @param rawPassword the raw password to verify
     * @param encodedPassword the encoded password to check against
     * @return true if the passwords match, false otherwise
     * @throws IllegalArgumentException if either parameter is null or the encoded
     *         password is in an invalid format
     */
    @Override
    public boolean matches(@NotNull CharSequence rawPassword, @NotNull String encodedPassword) {
        Argon2Hash decoded = doDecode(encodedPassword);
        byte[] decodedHash = decoded.getHash();
        byte[] hashBytes = new byte[decodedHash.length];

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(decoded.getParameters());
        generator.generateBytes(rawPassword.toString().toCharArray(), hashBytes);

        boolean equals = true;

        for (int i = 0; i < decodedHash.length; i++) {
            equals &= decodedHash[i] == hashBytes[i];
        }

        return equals;
    }

    /**
     * Encodes an Argon2 hash and its parameters into a standard string format.
     * Format: {@code type$version$memory$iterations$parallelism$salt$hash}
     * 
     * @param hash the hash bytes to encode
     * @param parameters the Argon2 parameters used to generate the hash
     * @return a string representation of the hash and parameters
     * @throws IllegalArgumentException if the parameters are invalid
     */
    private String doEncode(byte[] hash, Argon2Parameters parameters) throws IllegalArgumentException {
        StringBuilder stringBuilder = new StringBuilder();
        Argon2Type argon2Type = Argon2Type.fromVersion(parameters.getType());
        stringBuilder.append(argon2Type.readable)
                .append("$").append(parameters.getVersion())
                .append("$").append(parameters.getMemory())
                .append("$").append(parameters.getIterations())
                .append("$").append(parameters.getLanes())
                .append("$").append(b64encoder.encodeToString(parameters.getSalt()))
                .append("$").append(b64encoder.encodeToString(hash));
        return stringBuilder.toString();
    }

    /**
     * Decodes an encoded Argon2 hash string into its components.
     * Parses the standard format: {@code type$version$memory$iterations$parallelism$salt$hash}
     * 
     * @param encodedHash the encoded hash string to decode
     * @return an Argon2Hash object containing the hash and parameters
     * @throws IllegalArgumentException if the encoded hash format is invalid
     */
    private Argon2Hash doDecode(String encodedHash) throws IllegalArgumentException {
        String[] parts = sanitizeInput(encodedHash).split("\\$");
        if (parts.length != 7) {
            throw new IllegalArgumentException("Invalid encoded Argon2-hash");
        }
        Argon2Type argon2Type = Argon2Type.fromString(parts[0]);
        Argon2Parameters parameters = new Argon2Parameters.Builder(argon2Type.integerValue)
                .withVersion(Integer.parseInt(parts[1]))
                .withMemoryAsKB(Integer.parseInt(parts[2]))
                .withIterations(Integer.parseInt(parts[3]))
                .withParallelism(Integer.parseInt(parts[4]))
                .withSalt(b64decoder.decode(parts[5]))
                .build();

        return new Argon2Hash(b64decoder.decode(parts[6]), parameters);
    }

    /**
     * Enum representing the various Argon2 algorithm types.
     * <ul>
     *   <li>Argon2d: Maximizes resistance to GPU attacks but vulnerable to side-channels</li>
     *   <li>Argon2i: Designed for password hashing with protection against side-channels</li>
     *   <li>Argon2id: Hybrid mode combining Argon2i and Argon2d (recommended)</li>
     * </ul>
     */
    private enum Argon2Type {
        /**
         * Argon2d variant - optimized for maximum resistance against GPU attacks.
         */
        Argon2D("argon2d", Argon2Parameters.ARGON2_d),
        
        /**
         * Argon2i variant - designed for password hashing with side-channel protection.
         */
        Argon2I("argon2i", Argon2Parameters.ARGON2_i),
        
        /**
         * Argon2id variant - hybrid mode recommended for most use cases.
         */
        Argon2Id("argon2id", Argon2Parameters.ARGON2_id);

        private final String readable;
        private final int integerValue;

        /**
         * Constructs an Argon2Type enum value.
         * 
         * @param readable the string representation used in encoding
         * @param integerValue the BouncyCastle constant value
         */
        Argon2Type(String readable, int integerValue) {
            this.readable = readable;
            this.integerValue = integerValue;
        }

        /**
         * Finds an Argon2Type by its string representation.
         * 
         * @param type the string representation to look up
         * @return the matching Argon2Type
         * @throws IllegalArgumentException if the type is unknown
         */
        public static Argon2Type fromString(String type) {
            for (Argon2Type value : values()) {
                if (value.readable.equals(type)) {
                    return value;
                }
            }

            throw new IllegalArgumentException("Unknown argon2 type: " + type);
        }

        /**
         * Finds an Argon2Type by its integer value.
         * 
         * @param version the integer value to look up
         * @return the matching Argon2Type
         * @throws IllegalArgumentException if the version is unknown
         */
        public static Argon2Type fromVersion(int version) {
            for (Argon2Type value : values()) {
                if (value.integerValue == version) {
                    return value;
                }
            }

            throw new IllegalArgumentException("Unknown argon2 version: " + version);
        }
    }

    /**
     * Configuration properties for the Argon2 algorithm.
     * Controls the security and performance characteristics of the algorithm.
     */
    public record Properties(
            /**
             * Length of the hash output in bytes.
             * Recommended minimum: 16 bytes (128 bits).
             * Common values: 32 bytes (256 bits) or 64 bytes (512 bits).
             */
            int hashLength,
            
            /**
             * Number of threads to use for computation.
             * Higher values can speed up computation on multi-core systems.
             * Recommended: 1-4 depending on system resources.
             */
            int parallelism,
            
            /**
             * Memory requirement in KB.
             * Higher values increase security against GPU attacks but require more resources.
             * Recommended minimum: 64MB (1 << 16) for sensitive applications.
             */
            int memory,
            
            /**
             * Number of iterations (time cost).
             * Higher values increase security but make computation slower.
             * Recommended minimum: 1, typical values: 1-3.
             */
            int iterations
    ) {
    }

    /**
     * Container for an Argon2 hash and its associated parameters.
     * Used internally for password verification.
     */
    public static class Argon2Hash {

        private final byte[] hash;
        private final Argon2Parameters parameters;

        /**
         * Creates a new Argon2Hash with the specified hash and parameters.
         * Makes a defensive copy of the hash to prevent modification.
         * 
         * @param hash the hash bytes
         * @param parameters the Argon2 parameters used to generate the hash
         */
        Argon2Hash(byte[] hash, Argon2Parameters parameters) {
            this.hash = Arrays.clone(hash);
            this.parameters = parameters;
        }

        /**
         * Gets a copy of the hash bytes.
         * Always returns a defensive copy to prevent modification.
         * 
         * @return a copy of the hash bytes
         */
        public byte[] getHash() {
            return Arrays.clone(this.hash);
        }

        /**
         * Gets the Argon2 parameters.
         * 
         * @return the Argon2 parameters
         */
        public Argon2Parameters getParameters() {
            return this.parameters;
        }
    }
}