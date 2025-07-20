package com.wiredi.runtime.messaging;

import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class MessageHeaderTest {

    private enum TestEnum {
        VALUE1, VALUE2, VALUE3
    }

    @Test
    void testOfString() {
        // Arrange
        String name = "header-name";
        String value = "test-value";

        // Act
        MessageHeader header = MessageHeader.of(name, value);

        // Assert
        assertThat(header.name()).isEqualTo(name);
        assertThat(header.decodeToString()).isEqualTo(value);
    }

    @Test
    void testOfShort() {
        // Arrange
        String name = "header-name";
        short value = 42;

        // Act
        MessageHeader header = MessageHeader.of(name, value);

        // Assert
        assertThat(header.name()).isEqualTo(name);
        assertThat(header.decodeToShort()).isEqualTo(value);
    }

    @Test
    void testOfInt() {
        // Arrange
        String name = "header-name";
        int value = 42;

        // Act
        MessageHeader header = MessageHeader.of(name, value);

        // Assert
        assertThat(header.name()).isEqualTo(name);
        assertThat(header.decodeToInt()).isEqualTo(value);
    }

    @Test
    void testOfBoolean() {
        // Arrange
        String name = "header-name";
        boolean value = true;

        // Act
        MessageHeader header = MessageHeader.of(name, value);

        // Assert
        assertThat(header.name()).isEqualTo(name);
        assertThat(header.decodeToString()).isEqualTo("true");
    }

    @Test
    void testOfLong() {
        // Arrange
        String name = "header-name";
        long value = 42L;

        // Act
        MessageHeader header = MessageHeader.of(name, value);

        // Assert
        assertThat(header.name()).isEqualTo(name);
        assertThat(header.decodeToLong()).isEqualTo(value);
    }

    @Test
    void testOfFloat() {
        // Arrange
        String name = "header-name";
        float value = 42.5f;

        // Act
        MessageHeader header = MessageHeader.of(name, value);

        // Assert
        assertThat(header.name()).isEqualTo(name);
        assertThat(header.decodeToFloat()).isEqualTo(value);
    }

    @Test
    void testOfDouble() {
        // Arrange
        String name = "header-name";
        double value = 42.5;

        // Act
        MessageHeader header = MessageHeader.of(name, value);

        // Assert
        assertThat(header.name()).isEqualTo(name);
        assertThat(header.decodeToDouble()).isEqualTo(value);
    }

    @Test
    void testOfEnum() {
        // Arrange
        String name = "header-name";
        TestEnum value = TestEnum.VALUE2;

        // Act
        MessageHeader header = MessageHeader.of(name, value);

        // Assert
        assertThat(header.name()).isEqualTo(name);
        assertThat(header.decodeToEnum(TestEnum.class)).isEqualTo(value);
    }

    @Test
    void testOfInstant() {
        // Arrange
        String name = "header-name";
        Instant value = Instant.now();
        // Truncate to milliseconds since that's the precision used in MessageHeader.of(name, instant)
        Instant truncatedValue = Instant.ofEpochMilli(value.toEpochMilli());

        // Act
        MessageHeader header = MessageHeader.of(name, value);

        // Assert
        assertThat(header.name()).isEqualTo(name);
        assertThat(header.decodeToInstant()).isEqualTo(truncatedValue);
    }

    @Test
    void testOfInstantWithFormatter() {
        // Arrange
        String name = "header-name";
        Instant value = Instant.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

        // Act
        MessageHeader header = MessageHeader.of(name, value, formatter);

        // Assert
        assertThat(header.name()).isEqualTo(name);
        assertThat(header.decodeToInstant(formatter)).isEqualTo(value);
    }

    @Test
    void testDecodeToStringWithCharset() {
        // Arrange
        String name = "header-name";
        String value = "test-value";
        Charset charset = StandardCharsets.UTF_8;

        // Act
        MessageHeader header = MessageHeader.of(name, value);

        // Assert
        assertThat(header.decodeToString(charset)).isEqualTo(value);
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        MessageHeader header1 = MessageHeader.of("name", "value");
        MessageHeader header2 = MessageHeader.of("name", "value");
        MessageHeader header3 = MessageHeader.of("different", "value");
        MessageHeader header4 = MessageHeader.of("name", "different");

        // Assert
        assertThat(header1).isEqualTo(header2);
        assertThat(header1.hashCode()).isEqualTo(header2.hashCode());

        assertThat(header1).isNotEqualTo(header3);
        assertThat(header1).isNotEqualTo(header4);

        assertThat(header1).isNotEqualTo(null);
        assertThat(header1).isNotEqualTo("not a header");
    }
}
