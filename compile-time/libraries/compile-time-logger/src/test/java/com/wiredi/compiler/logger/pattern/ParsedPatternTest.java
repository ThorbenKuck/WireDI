package com.wiredi.compiler.logger.pattern;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ParsedPatternTest {

    @Test
    public void test() {
        // Arrange
        String pattern = "[${maxLength:5}] [${maxLengthRight:r5}] [${exactLengthShorter:30.30}] [${exactLengthShorterRight:r30.30}] [${exactLength:30.30}] [${exactLengthRight:r30.30}] : ${message}";
        ParsedPattern parse = ParsedPattern.parse(pattern);

        // Act
        CompiledLogPattern compile = parse.compile(Map.of(
                "maxLength", "123456789",
                "maxLengthRight", "123456789",
                "exactLengthShorter", "123456789",
                "exactLengthShorterRight", "123456789",
                "exactLength", "com.wiredi.compiler.logger.pattern.ParsedPatternTest",
                "exactLengthRight", "com.wiredi.compiler.logger.pattern.ParsedPatternTest",
                "message", "A log message")
        );

        // Assert
        String format = compile.format();
        assertThat(format).isEqualTo("[12345] [56789] [123456789                     ] [                     123456789] [com.wiredi.compiler.logger.pat] [gger.pattern.ParsedPatternTest] : A log message");
    }
}