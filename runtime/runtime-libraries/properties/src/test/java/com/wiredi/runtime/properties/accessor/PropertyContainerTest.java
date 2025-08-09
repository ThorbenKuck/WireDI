package com.wiredi.runtime.properties.accessor;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PropertyContainerTest {

    @Test
    public void propertyAccessorWorks() {
        // Arrange
        TestProperties properties = new TestProperties("first", "second");

        // Act
        Map<String, Object> map = properties.toMap();

        // Assert
        assertThat(map).containsEntry("first", "first");
        assertThat(map).containsEntry("second", "second".getBytes());
    }

    @Test
    public void propertyAccessorWorksWithAllNullValues() {
        // Arrange
        TestProperties properties = new TestProperties(null, null);

        // Act
        Map<String, Object> map = properties.toMap();

        // Assert
        assertThat(map).containsEntry("first", "default-first");
        assertThat(map).containsEntry("second", "default-second".getBytes());
    }

    static class TestProperties extends PropertyContainer {

        @Nullable
        private final String first;
        @Nullable
        private final String second;

        TestProperties(
                @Nullable String first,
                @Nullable String second
        ) {
            this.first = first;
            this.second = second;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> result = new HashMap<>();

            property(first)
                    .or("default-first")
                    .applyTo(it -> result.put("first", it));
            property(second)
                    .or("default-second")
                    .map(String::getBytes)
                    .applyTo(it -> result.put("second", it));

            return result;
        }
    }
}