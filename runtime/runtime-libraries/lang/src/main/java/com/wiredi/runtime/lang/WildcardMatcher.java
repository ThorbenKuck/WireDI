package com.wiredi.runtime.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A high-performance wildcard pattern matcher that provides flexible string matching capabilities using wildcard characters.
 * This class is immutable and thread-safe, making it suitable for use as a shared constant or cached matcher instance.
 *
 * <p>The matcher supports three types of wildcard characters that enable various matching patterns.
 *
 * <p>The single asterisk wildcard ({@code *}) has different behavior depending on its position in the pattern.
 * When used alone at the start ({@code *suffix}), end ({@code prefix*}), or both ends ({@code *substring*}) of a pattern, the asterisk matches any sequence of characters including dots.
 * When used in more complex positions within a pattern (such as {@code com.*.Logger}), the asterisk matches any sequence of characters except dots, effectively limiting matches to within dot-separated segments.
 * This dual behavior provides both convenience for simple patterns and precision for complex hierarchical matching scenarios.
 *
 * <p>The double asterisk wildcard ({@code **}) always matches any sequence of characters including dots, regardless of its position in the pattern.
 * This enables matching across arbitrary levels of hierarchy in dot-separated names.
 * For example, {@code com.wiredi.**} matches {@code com.wiredi.Logger}, {@code com.wiredi.logging.Logger}, and {@code com.wiredi.a.b.c.Logger}.
 *
 * <p>The question mark wildcard ({@code ?}) matches exactly one character, but excludes the dot character from matching.
 * This provides precise single-character matching while respecting dot-separated segment boundaries.
 * For example, {@code Logger?} matches {@code Logger1} or {@code LoggerA} but not {@code Logger} (too short) or {@code Logger.X} (dot not allowed).
 *
 * <p>This implementation employs multiple optimization strategies to maximize performance for common pattern types.
 * Simple patterns receive highly optimized implementations that use basic string operations instead of regular expressions.
 * Patterns without wildcards use exact string comparison.
 * Patterns ending with a single asterisk use prefix matching via {@code startsWith()}.
 * Patterns starting with a single asterisk use suffix matching via {@code endsWith()}.
 * Patterns with asterisks at both ends use substring matching via {@code contains()}.
 * All other patterns fall back to compiled regular expressions for complex matching logic.
 * The optimization strategy is selected once during pattern compilation, ensuring minimal overhead for subsequent match operations.
 *
 * <p>All pattern matching is case-sensitive by design, meaning {@code Logger} and {@code logger} are treated as distinct values.
 * Special regular expression characters appearing in the pattern (such as {@code +}, {@code (}, {@code )}, {@code [}, {@code ]}, {@code ^}, {@code $}) are automatically escaped and treated as literal characters rather than regex metacharacters.
 *
 * <p>Common usage examples demonstrate the variety of patterns supported by this matcher:
 * <pre>{@code
 * // Exact match - matches only "Logger"
 * WildcardMatcher.compile("Logger").matches("Logger"); // true
 *
 * // Prefix match - matches anything starting with "com.wiredi."
 * WildcardMatcher.compile("com.wiredi.*").matches("com.wiredi.Logger"); // true
 * WildcardMatcher.compile("com.wiredi.*").matches("com.wiredi.sub.Logger"); // true (includes dots)
 *
 * // Suffix match - matches anything ending with "Logger"
 * WildcardMatcher.compile("*Logger").matches("MyLogger"); // true
 * WildcardMatcher.compile("*Logger").matches("com.wiredi.Logger"); // true (includes dots)
 *
 * // Contains match - matches anything containing "logging"
 * WildcardMatcher.compile("*logging*").matches("myloggingservice"); // true
 *
 * // Complex pattern with asterisk between segments (dot-aware)
 * WildcardMatcher.compile("com.*.Logger").matches("com.wiredi.Logger"); // true
 * WildcardMatcher.compile("com.*.Logger").matches("com.my.pkg.Logger"); // false (cannot cross dots)
 *
 * // Double asterisk matches across all segments
 * WildcardMatcher.compile("com.**.Logger").matches("com.wiredi.logging.Logger"); // true
 *
 * // Question mark for single character matching
 * WildcardMatcher.compile("Logger?").matches("Logger1"); // true
 * WildcardMatcher.compile("Logger?").matches("Logger.X"); // false (dot not allowed)
 *
 * // Match everything
 * WildcardMatcher.compile("*").matches("anything"); // true
 * }</pre>
 *
 * <p>The matcher treats null patterns as equivalent to empty string patterns, both matching only empty input strings.
 *
 * <p>Since instances are immutable, they can be safely shared across threads and reused for multiple match operations without any synchronization overhead.
 * Consider compiling patterns once and storing them as constants when they will be used repeatedly throughout your application.
 *
 * @see java.util.regex.Pattern
 */
public final class WildcardMatcher {

    private final String pattern;
    private final MatchStrategy strategy;

    private WildcardMatcher(String pattern, MatchStrategy strategy) {
        this.pattern = pattern;
        this.strategy = strategy;
    }

    /**
     * Compiles a wildcard pattern into a matcher instance optimized for repeated matching operations.
     * The compilation process analyzes the pattern structure and selects the most efficient matching strategy available.
     *
     * <p>Pattern compilation is a one-time cost that enables fast matching operations afterwards.
     * The resulting matcher instance can be reused safely across multiple threads and should be cached when possible to avoid repeated compilation overhead.
     *
     * <p>The pattern syntax supports several wildcard types that can be combined freely.
     * The single asterisk wildcard behaves differently based on its position: when used as a simple prefix ({@code prefix*}), suffix ({@code *suffix}), or contains pattern ({@code *substring*}), it matches any characters including dots; when used in complex patterns with other literal characters or wildcards, it matches any characters except dots.
     * The double asterisk wildcard always matches any characters including dots, allowing matches across segment boundaries.
     * The question mark wildcard always matches exactly one character, excluding dots.
     *
     * <p>Simple patterns receive optimized implementations that avoid regular expression overhead.
     * Patterns without any wildcards use exact string comparison.
     * Patterns with a single asterisk at the end use prefix matching.
     * Patterns with a single asterisk at the start use suffix matching.
     * Patterns with asterisks at both ends use substring matching.
     * Complex patterns with multiple wildcards or special combinations use compiled regular expressions.
     *
     * <p>The method accepts null patterns and treats them identically to empty string patterns, both matching only empty input strings.
     * This behavior provides convenient null-safety without requiring explicit null checks in calling code.
     *
     * @param pattern the wildcard pattern to compile, or null to create a matcher that only accepts empty strings
     * @return a compiled matcher instance ready for matching operations, never null
     * @see #matches(String)
     */
    public static WildcardMatcher compile(@Nullable String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return new WildcardMatcher(pattern, new ExactMatchStrategy(""));
        }

        // Optimize common cases
        if (!pattern.contains("*") && !pattern.contains("?")) {
            return new WildcardMatcher(pattern, new ExactMatchStrategy(pattern));
        }

        if (pattern.equals("*")) {
            return new WildcardMatcher(pattern, new MatchAllStrategy());
        }

        // Check if pattern can use optimized strategies
        // Important: We need to check for ** first, then *
        boolean hasDoubleWildcard = pattern.contains("**");
        boolean hasQuestionMark = pattern.contains("?");

        // Only use optimized strategies if there's no ** or ?
        if (!hasDoubleWildcard && !hasQuestionMark) {
            // Pattern has only single * wildcards

            // Prefix: abc* (no * except at end)
            if (pattern.endsWith("*") && countOccurrences(pattern, '*') == 1) {
                return new WildcardMatcher(pattern, new PrefixStrategy(pattern.substring(0, pattern.length() - 1)));
            }

            // Suffix: *abc (no * except at start)
            if (pattern.startsWith("*") && countOccurrences(pattern, '*') == 1) {
                return new WildcardMatcher(pattern, new SuffixStrategy(pattern.substring(1)));
            }

            // Contains: *abc* (no * in middle)
            if (pattern.startsWith("*") && pattern.endsWith("*") && countOccurrences(pattern, '*') == 2) {
                return new WildcardMatcher(pattern, new ContainsStrategy(pattern.substring(1, pattern.length() - 1)));
            }
        }

        // Complex pattern - use regex
        return new WildcardMatcher(pattern, new RegexMatchStrategy(toRegex(pattern)));
    }

    private static int countOccurrences(String str, char ch) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ch) {
                count++;
            }
        }
        return count;
    }

    private static String toRegex(String pattern) {
        StringBuilder regex = new StringBuilder("^");

        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            switch (c) {
                case '*':
                    // Check for ** (match across package boundaries)
                    if (i + 1 < pattern.length() && pattern.charAt(i + 1) == '*') {
                        regex.append(".*");
                        i++; // Skip next *
                    } else {
                        // Single * matches zero or more characters except dots
                        regex.append("[^.]*");
                    }
                    break;
                case '?':
                    // ? matches exactly one character except dots
                    regex.append("[^.]");
                    break;
                case '.':
                case '\\':
                case '(':
                case ')':
                case '[':
                case ']':
                case '{':
                case '}':
                case '^':
                case '$':
                case '|':
                case '+':
                    regex.append('\\').append(c);
                    break;
                default:
                    regex.append(c);
            }
        }

        regex.append("$");
        return regex.toString();
    }

    /**
     * Tests whether the provided input string matches this compiled wildcard pattern.
     * The matching operation executes using the optimized strategy selected during pattern compilation, ensuring efficient performance even when called repeatedly.
     *
     * <p>The method performs case-sensitive matching, meaning that character case must match exactly between the pattern and input.
     * Wildcard characters in the pattern follow their defined semantics, with behavior varying based on the pattern structure and optimization strategy used.
     *
     * <p>This operation is thread-safe and can be called concurrently from multiple threads without any synchronization overhead.
     * The matcher instance maintains no mutable state, making it safe for unlimited concurrent use.
     *
     * @param input the string to test against the pattern, must not be null
     * @return true if the input matches the pattern according to wildcard matching rules, false otherwise
     * @throws NullPointerException if input is null
     * @see #compile(String)
     */
    public boolean matches(@NotNull String input) {
        return strategy.matches(input);
    }

    /**
     * Returns the original wildcard pattern string that was used to compile this matcher.
     * This method is useful for debugging, logging, or displaying the pattern to users.
     *
     * <p>For matchers created from null patterns, this method returns null rather than an empty string, preserving the original input value.
     *
     * @return the pattern string provided during compilation, or null if the matcher was compiled from a null pattern
     */
    @Nullable
    public String getPattern() {
        return pattern;
    }

    /**
     * Compares this matcher with another object for equality.
     * Two matchers are considered equal if they were compiled from identical pattern strings, including the case where both patterns are null.
     *
     * <p>This equality contract enables matchers to be used effectively in collections such as sets and maps, and supports caching scenarios where matchers with identical patterns should be treated as interchangeable.
     *
     * @param obj the object to compare with this matcher
     * @return true if the given object is a WildcardMatcher with an identical pattern, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WildcardMatcher)) return false;
        WildcardMatcher that = (WildcardMatcher) obj;
        return Objects.equals(pattern, that.pattern);
    }

    /**
     * Returns a hash code value for this matcher based on its pattern string.
     * Matchers compiled from identical patterns will produce identical hash codes, satisfying the general contract of hashCode in relation to equals.
     *
     * <p>This implementation enables efficient use of matchers in hash-based collections such as HashMap and HashSet.
     *
     * @return a hash code value for this matcher
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(pattern);
    }

    /**
     * Returns a string representation of this matcher suitable for debugging and logging.
     * The representation includes the class name and the pattern string, providing clear identification of the matcher configuration.
     *
     * @return a string representation of this matcher in the format "WildcardMatcher{pattern='...'}"
     */
    @Override
    public String toString() {
        return "WildcardMatcher{pattern='" + pattern + "'}";
    }

    // Strategy interface for different matching approaches
    private interface MatchStrategy {
        boolean matches(String input);
    }

    // Exact match - O(n)
    private static class ExactMatchStrategy implements MatchStrategy {
        private final String expected;

        ExactMatchStrategy(String expected) {
            this.expected = expected;
        }

        @Override
        public boolean matches(String input) {
            return expected.equals(input);
        }
    }

    // Match all (including dots) - O(1)
    private static class MatchAllStrategy implements MatchStrategy {
        @Override
        public boolean matches(String input) {
            return true;
        }
    }

    // Prefix match (no dots) - O(n)
    private static class PrefixStrategy implements MatchStrategy {
        private final String prefix;

        PrefixStrategy(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public boolean matches(String input) {
            return input.startsWith(prefix);
        }
    }

    // Suffix match (no dots) - O(n)
    private static class SuffixStrategy implements MatchStrategy {
        private final String suffix;

        SuffixStrategy(String suffix) {
            this.suffix = suffix;
        }

        @Override
        public boolean matches(String input) {
            return input.endsWith(suffix);
        }
    }

    // Contains match (no dots) - O(n)
    private static class ContainsStrategy implements MatchStrategy {
        private final String substring;

        ContainsStrategy(String substring) {
            this.substring = substring;
        }

        @Override
        public boolean matches(String input) {
            return input.contains(substring);
        }
    }

    // Complex pattern - uses regex
    private static class RegexMatchStrategy implements MatchStrategy {
        private final Pattern pattern;

        RegexMatchStrategy(String regex) {
            this.pattern = Pattern.compile(regex);
        }

        @Override
        public boolean matches(String input) {
            return pattern.matcher(input).matches();
        }
    }
}
