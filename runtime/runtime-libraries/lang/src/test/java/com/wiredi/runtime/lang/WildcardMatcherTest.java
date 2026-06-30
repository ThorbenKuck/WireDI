package com.wiredi.runtime.lang;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WildcardMatcher Tests")
class WildcardMatcherTest {

    @Nested
    @DisplayName("Exact Match Strategy Tests")
    class ExactMatchStrategyTests {

        @Test
        @DisplayName("should match exact strings")
        void shouldMatchExactStrings() {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile("Logger");

            // Act & Assert
            assertThat(matcher.matches("Logger")).isTrue();
            assertThat(matcher.matches("logger")).isFalse();
            assertThat(matcher.matches("Logger2")).isFalse();
            assertThat(matcher.matches("MyLogger")).isFalse();
            assertThat(matcher.matches("")).isFalse();
        }

        @Test
        @DisplayName("should match exact qualified names")
        void shouldMatchExactQualifiedNames() {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile("com.wiredi.logging.Logger");

            // Act & Assert
            assertThat(matcher.matches("com.wiredi.logging.Logger")).isTrue();
            assertThat(matcher.matches("com.wiredi.Logger")).isFalse();
            assertThat(matcher.matches("Logger")).isFalse();
        }

        @Test
        @DisplayName("should handle empty pattern")
        void shouldHandleEmptyPattern() {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile("");

            // Act & Assert
            assertThat(matcher.matches("")).isTrue();
            assertThat(matcher.matches("anything")).isFalse();
        }

        @Test
        @DisplayName("should handle null pattern")
        void shouldHandleNullPattern() {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile(null);

            // Act & Assert
            assertThat(matcher.matches("")).isTrue();
            assertThat(matcher.matches("anything")).isFalse();
        }
    }

    @Nested
    @DisplayName("Match All Strategy Tests")
    class MatchAllStrategyTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "",
                "Logger",
                "com.wiredi.Logger",
                "com.wiredi.logging.Logger",
                "anything at all",
                "123456"
        })
        @DisplayName("should match everything with single asterisk")
        void shouldMatchEverythingWithSingleAsterisk(String input) {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile("*");

            // Act & Assert
            assertThat(matcher.matches(input)).isTrue();
        }
    }

    @Nested
    @DisplayName("Starts With Strategy Tests")
    class StartsWithStrategyTests {

        @ParameterizedTest
        @CsvSource({
                "com.wiredi.*, com.wiredi.Logger, true",
                "com.wiredi.*, com.wiredi.LoggingAccessor, true",
                "com.wiredi.*, com.wiredi.sub.Logger, true",
                "com.wiredi.*, com.other.Logger, false",
                "com.wiredi.*, Logger, false",
                "com.wiredi.*, com.wiredi., true"
        })
        @DisplayName("should match prefix patterns")
        void shouldMatchPrefixPatterns(String pattern, String input, boolean expected) {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile(pattern);

            // Act & Assert
            assertThat(matcher.matches(input))
                    .as("Pattern '%s' matching '%s' should be %s", pattern, input, expected)
                    .isEqualTo(expected);
        }

        @Test
        @DisplayName("should not match when prefix doesn't match")
        void shouldNotMatchWhenPrefixDoesNotMatch() {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile("com.wiredi.*");

            // Act & Assert
            assertThat(matcher.matches("org.wiredi.Logger")).isFalse();
            assertThat(matcher.matches("com.other.Logger")).isFalse();
            assertThat(matcher.matches("Logger")).isFalse();
        }
    }

    @Nested
    @DisplayName("Ends With Strategy Tests")
    class EndsWithStrategyTests {

        @ParameterizedTest
        @CsvSource({
                "*Logger, Logger, true",
                "*Logger, MyLogger, true",
                "*Logger, CustomLogger, true",
                "*Logger, com.wiredi.Logger, true",
                "*Logger, LoggerImpl, false",
                "*Logger, logger, false"
        })
        @DisplayName("should match suffix patterns")
        void shouldMatchSuffixPatterns(String pattern, String input, boolean expected) {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile(pattern);

            // Act & Assert
            assertThat(matcher.matches(input))
                    .as("Pattern '%s' matching '%s' should be %s", pattern, input, expected)
                    .isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Contains Strategy Tests")
    class ContainsStrategyTests {

        @ParameterizedTest
        @CsvSource({
                "*logging*, logging, true",
                "*logging*, mylogging, true",
                "*logging*, loggingservice, true",
                "*logging*, myloggingservice, true",
                "*logging*, com.wiredi.logging, true",
                "*logging*, Logger, false",
                "*logging*, LOGGING, false"
        })
        @DisplayName("should match contains patterns")
        void shouldMatchContainsPatterns(String pattern, String input, boolean expected) {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile(pattern);

            // Act & Assert
            assertThat(matcher.matches(input))
                    .as("Pattern '%s' matching '%s' should be %s", pattern, input, expected)
                    .isEqualTo(expected);
        }

        @Test
        @DisplayName("should not match when substring is not present")
        void shouldNotMatchWhenSubstringIsNotPresent() {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile("*logging*");

            // Act & Assert
            assertThat(matcher.matches("Logger")).isFalse();
            assertThat(matcher.matches("Service")).isFalse();
            assertThat(matcher.matches("")).isFalse();
        }
    }

    @Nested
    @DisplayName("Single Wildcard (*) in Complex Patterns")
    class SingleWildcardComplexTests {

        @ParameterizedTest
        @CsvSource({
                "com.*.Logger, com.wiredi.Logger, true",
                "com.*.Logger, com.other.Logger, true",
                "com.*.Logger, com.my.pkg.Logger, false",
                "com.*.Logger, org.wiredi.Logger, false",
                "com.*.Logger, com.Logger, false"
        })
        @DisplayName("should match middle wildcards within package boundaries")
        void shouldMatchMiddleWildcardsWithinPackageBoundaries(String pattern, String input, boolean expected) {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile(pattern);

            // Act & Assert
            assertThat(matcher.matches(input))
                    .as("Pattern '%s' matching '%s' should be %s", pattern, input, expected)
                    .isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "*.logging.*, com.logging.Logger, true",
                "*.logging.*, org.logging.Service, true",
                "*.logging.*, logging.Util, false",
                "*.logging.*, com.logging, false",
                "*.logging.*, com.wiredi.logging.sub.Logger, true"
        })
        @DisplayName("should match multiple wildcards")
        void shouldMatchMultipleWildcards(String pattern, String input, boolean expected) {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile(pattern);

            // Act & Assert
            assertThat(matcher.matches(input))
                    .as("Pattern '%s' matching '%s' should be %s", pattern, input, expected)
                    .isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Double Wildcard (**) Tests")
    class DoubleWildcardTests {

        @ParameterizedTest
        @CsvSource({
                "com.wiredi.**, com.wiredi.Logger, true",
                "com.wiredi.**, com.wiredi.logging.Logger, true",
                "com.wiredi.**, com.wiredi.logging.sub.Logger, true",
                "com.wiredi.**, com.wiredi.a.b.c.d.Logger, true",
                "com.wiredi.**, com.other.Logger, false",
                "com.wiredi.**, Logger, false"
        })
        @DisplayName("should match across package boundaries")
        void shouldMatchAcrossPackageBoundaries(String pattern, String input, boolean expected) {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile(pattern);

            // Act & Assert
            assertThat(matcher.matches(input))
                    .as("Pattern '%s' matching '%s' should be %s", pattern, input, expected)
                    .isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "**Logger, Logger, true",
                "**Logger, com.Logger, true",
                "**Logger, com.wiredi.Logger, true",
                "**Logger, com.wiredi.logging.Logger, true",
                "**Logger, LoggerImpl, false",
                "**Logger, MyLogger, true"
        })
        @DisplayName("should match suffix across packages")
        void shouldMatchSuffixAcrossPackages(String pattern, String input, boolean expected) {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile(pattern);

            // Act & Assert
            assertThat(matcher.matches(input))
                    .as("Pattern '%s' matching '%s' should be %s", pattern, input, expected)
                    .isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "com.**.Logger, com.Logger, false",
                "com.**.Logger, com.wiredi.Logger, true",
                "com.**.Logger, com.wiredi.logging.Logger, true",
                "com.**.Logger, com.a.b.c.d.Logger, true",
                "com.**.Logger, org.wiredi.Logger, false",
                "com.**.Logger, Logger, false"
        })
        @DisplayName("should match middle wildcard across packages")
        void shouldMatchMiddleWildcardAcrossPackages(String pattern, String input, boolean expected) {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile(pattern);

            // Act & Assert
            assertThat(matcher.matches(input))
                    .as("Pattern '%s' matching '%s' should be %s", pattern, input, expected)
                    .isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "com.**.logging.**, com.wiredi.logging.Logger, true",
                "com.**.logging.**, com.other.logging.Service, true",
                "com.**.logging.**, com.a.b.logging.c.d.Util, true",
                "com.**.logging.**, org.logging.Logger, false",
                "com.**.logging.**, com.wiredi.Logger, false"
        })
        @DisplayName("should match multiple double wildcards")
        void shouldMatchMultipleDoubleWildcards(String pattern, String input, boolean expected) {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile(pattern);

            // Act & Assert
            assertThat(matcher.matches(input))
                    .as("Pattern '%s' matching '%s' should be %s", pattern, input, expected)
                    .isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Question Mark (?) Wildcard Tests")
    class QuestionMarkTests {

        @ParameterizedTest
        @CsvSource({
                "Logger?, Logger1, true",
                "Logger?, Logger2, true",
                "Logger?, LoggerA, true",
                "Logger?, Logger, false",
                "Logger?, Logger12, false",
                "Logger?, Logger.X, false"
        })
        @DisplayName("should match single character")
        void shouldMatchSingleCharacter(String pattern, String input, boolean expected) {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile(pattern);

            // Act & Assert
            assertThat(matcher.matches(input))
                    .as("Pattern '%s' matching '%s' should be %s", pattern, input, expected)
                    .isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "Log?er, Logger, true",
                "Log?er, Logxer, true",
                "Log?er, Log9er, true",
                "Log?er, Loger, false",
                "Log?er, Logager, false",
                "Log?er, Log.er, false"
        })
        @DisplayName("should match question mark in middle of pattern")
        void shouldMatchQuestionMarkInMiddleOfPattern(String pattern, String input, boolean expected) {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile(pattern);

            // Act & Assert
            assertThat(matcher.matches(input))
                    .as("Pattern '%s' matching '%s' should be %s", pattern, input, expected)
                    .isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "com.?.Logger, com.a.Logger, true",
                "com.?.Logger, com.wiredi.Logger, false",
                "com.?.Logger, com..Logger, false"
        })
        @DisplayName("should match single package segment with question mark")
        void shouldMatchSinglePackageSegmentWithQuestionMark(String pattern, String input, boolean expected) {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile(pattern);

            // Act & Assert
            assertThat(matcher.matches(input))
                    .as("Pattern '%s' matching '%s' should be %s", pattern, input, expected)
                    .isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Mixed Wildcard Tests")
    class MixedWildcardTests {

        @ParameterizedTest
        @CsvSource({
                "com.*.Log?er, com.wiredi.Logger, true",
                "com.*.Log?er, com.other.Logxer, true",
                "com.*.Log?er, com.wiredi.Loger, false",
                "com.*.Log?er, com.a.b.Logger, false"
        })
        @DisplayName("should combine single wildcard with question mark")
        void shouldCombineSingleWildcardWithQuestionMark(String pattern, String input, boolean expected) {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile(pattern);

            // Act & Assert
            assertThat(matcher.matches(input))
                    .as("Pattern '%s' matching '%s' should be %s", pattern, input, expected)
                    .isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "com.**.Log?er, com.wiredi.Logger, true",
                "com.**.Log?er, com.wiredi.logging.Logger, true",
                "com.**.Log?er, com.a.b.c.Logger, true",
                "com.**.Log?er, com.wiredi.Loger, false",
                "com.**.Log?er, org.wiredi.Logger, false"
        })
        @DisplayName("should combine double wildcard with question mark")
        void shouldCombineDoubleWildcardWithQuestionMark(String pattern, String input, boolean expected) {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile(pattern);

            // Act & Assert
            assertThat(matcher.matches(input))
                    .as("Pattern '%s' matching '%s' should be %s", pattern, input, expected)
                    .isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "com.*.*Log?er, com.a.bLogger, true",
                "com.*.*Log?er, com.wiredi.MyLogger, true",
                "com.*.*Log?er, com.a.Logger, true",
                "com.*.*Log?er, com.a.b.c.Logger, false"
        })
        @DisplayName("should handle complex mixed patterns")
        void shouldHandleComplexMixedPatterns(String pattern, String input, boolean expected) {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile(pattern);

            // Act & Assert
            assertThat(matcher.matches(input))
                    .as("Pattern '%s' matching '%s' should be %s", pattern, input, expected)
                    .isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Special Characters Escaping Tests")
    class SpecialCharactersTests {

        @Test
        @DisplayName("should handle patterns with dots correctly")
        void shouldHandlePatternsWithDotsCorrectly() {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile("com.wiredi.Logger");

            // Act & Assert
            assertThat(matcher.matches("com.wiredi.Logger")).isTrue();
            assertThat(matcher.matches("comXwirediXLogger")).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"+", "(", ")", "[", "]", "{", "}", "^", "$", "|"})
        @DisplayName("should escape regex special characters")
        void shouldEscapeRegexSpecialCharacters(String specialChar) {
            // Arrange
            String pattern = "Test" + specialChar + "Class";
            WildcardMatcher matcher = WildcardMatcher.compile(pattern);

            // Act & Assert
            assertThat(matcher.matches("Test" + specialChar + "Class")).isTrue();
            assertThat(matcher.matches("TestClass")).isFalse();
        }

        @Test
        @DisplayName("should handle backslash in pattern")
        void shouldHandleBackslashInPattern() {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile("Test\\Class");

            // Act & Assert
            assertThat(matcher.matches("Test\\Class")).isTrue();
            assertThat(matcher.matches("TestClass")).isFalse();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("should handle consecutive asterisks")
        void shouldHandleConsecutiveAsterisks() {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile("com.***Logger");

            // Act & Assert
            assertThat(matcher.matches("com.Logger")).isTrue();
            assertThat(matcher.matches("com.wiredi.Logger")).isTrue();
            assertThat(matcher.matches("com.wiredi.logging.Logger")).isTrue();
        }

        @Test
        @DisplayName("should handle pattern with only wildcards and dots")
        void shouldHandlePatternWithOnlyWildcardsAndDots() {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile("*.*.*");

            // Act & Assert
            assertThat(matcher.matches("com.wiredi.Logger")).isTrue();
            assertThat(matcher.matches("a.b.c")).isTrue();
            assertThat(matcher.matches("a.b")).isFalse();
            assertThat(matcher.matches("a.b.c.d")).isFalse();
        }

        @Test
        @DisplayName("should handle very long input strings")
        void shouldHandleVeryLongInputStrings() {
            // Arrange
            String longPackage = "com.company.project.module.submodule.package1.package2.package3.package4.ClassName";
            WildcardMatcher matcher = WildcardMatcher.compile("com.company.**ClassName");

            // Act & Assert
            assertThat(matcher.matches(longPackage)).isTrue();
        }

        @Test
        @DisplayName("should handle empty input")
        void shouldHandleEmptyInput() {
            // Arrange
            WildcardMatcher exactMatcher = WildcardMatcher.compile("Logger");
            WildcardMatcher wildcardMatcher = WildcardMatcher.compile("*");

            // Act & Assert
            assertThat(exactMatcher.matches("")).isFalse();
            assertThat(wildcardMatcher.matches("")).isTrue();
        }

        @Test
        @DisplayName("should handle pattern with trailing dots")
        void shouldHandlePatternWithTrailingDots() {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile("com.wiredi.");

            // Act & Assert
            assertThat(matcher.matches("com.wiredi.")).isTrue();
            assertThat(matcher.matches("com.wiredi")).isFalse();
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("should use optimized strategy for exact match")
        void shouldUseOptimizedStrategyForExactMatch() {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile("com.wiredi.logging.Logger");
            String input = "com.wiredi.logging.Logger";

            // Act
            long startTime = System.nanoTime();
            for (int i = 0; i < 100000; i++) {
                matcher.matches(input);
            }
            long endTime = System.nanoTime();

            // Assert - Should be very fast (< 50ms for 100k iterations)
            long duration = (endTime - startTime) / 1_000_000; // Convert to ms
            assertThat(duration).isLessThan(50);
        }

        @Test
        @DisplayName("should use optimized strategy for prefix match")
        void shouldUseOptimizedStrategyForPrefixMatch() {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile("com.wiredi.logging.*");

            // Act & Assert
            assertThat(matcher.matches("com.wiredi.logging.Logger")).isTrue();
            assertThat(matcher.matches("com.wiredi.logging.LoggingAccessor")).isTrue();
        }

        @Test
        @DisplayName("should handle many iterations efficiently")
        void shouldHandleManyIterationsEfficiently() {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile("com.**.Logger");
            String[] inputs = {
                    "com.wiredi.Logger",
                    "com.wiredi.logging.Logger",
                    "com.other.Logger",
                    "org.Logger"
            };

            // Act
            long startTime = System.nanoTime();
            for (int i = 0; i < 10000; i++) {
                for (String input : inputs) {
                    matcher.matches(input);
                }
            }
            long endTime = System.nanoTime();

            // Assert - Should complete in reasonable time (< 100ms for 40k matches)
            long duration = (endTime - startTime) / 1_000_000; // Convert to ms
            assertThat(duration).isLessThan(100);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("should provide meaningful toString")
        void shouldProvideMeaningfulToString() {
            // Arrange
            WildcardMatcher matcher = WildcardMatcher.compile("com.wiredi.*");

            // Act
            String result = matcher.toString();

            // Assert
            assertThat(result)
                    .contains("WildcardMatcher")
                    .contains("com.wiredi.*");
        }
    }
}