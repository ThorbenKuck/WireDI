package com.wiredi.runtime.properties.accessor;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for {@link PropertyAccessor} interface and its implementations.
 * This test covers the main functionality of PropertyAccessor, including:
 * - Creation of PropertyAccessor instances
 * - Mapping values
 * - Applying consumers
 * - Providing fallback values
 * - Retrieving values
 * - Chaining operations
 */
class PropertyAccessorTest {

    @Test
    void testCreationWithNonNullValue() {
        // Arrange
        String value = "test";

        // Act
        PropertyAccessor<String> accessor = PropertyAccessor.of(value);

        // Assert
        assertThat(accessor).isInstanceOf(SimplePropertyAccessor.class);
        assertThat(accessor.get()).isEqualTo(value);
    }

    @Test
    void testCreationWithNullValue() {
        // Arrange & Act
        PropertyAccessor<String> accessor = PropertyAccessor.of(null);

        // Assert
        assertThat(accessor).isInstanceOf(EmptyPropertyAccessor.class);
        assertThat(accessor.get()).isNull();
    }

    @Test
    void testEmptyCreation() {
        // Arrange & Act
        PropertyAccessor<String> accessor = PropertyAccessor.empty();

        // Assert
        assertThat(accessor).isInstanceOf(EmptyPropertyAccessor.class);
        assertThat(accessor.get()).isNull();
    }

    @Test
    void testMapWithNonNullValue() {
        // Arrange
        String value = "test";
        PropertyAccessor<String> accessor = PropertyAccessor.of(value);

        // Act
        PropertyAccessor<Integer> result = accessor.map(String::length);

        // Assert
        assertThat(result).isInstanceOf(SimplePropertyAccessor.class);
        assertThat(result.get()).isEqualTo(4);
    }

    @Test
    void testMapWithNullResult() {
        // Arrange
        String value = "test";
        PropertyAccessor<String> accessor = PropertyAccessor.of(value);

        // Act
        PropertyAccessor<String> result = accessor.map(s -> null);

        // Assert
        assertThat(result).isInstanceOf(EmptyPropertyAccessor.class);
        assertThat(result.get()).isNull();
    }

    @Test
    void testMapWithEmptyAccessor() {
        // Arrange
        PropertyAccessor<String> accessor = PropertyAccessor.empty();

        // Act
        PropertyAccessor<Integer> result = accessor.map(String::length);

        // Assert
        assertThat(result).isInstanceOf(EmptyPropertyAccessor.class);
        assertThat(result.get()).isNull();
    }

    @Test
    void testApplyToWithNonNullValue() {
        // Arrange
        String value = "test";
        PropertyAccessor<String> accessor = PropertyAccessor.of(value);
        AtomicReference<String> consumed = new AtomicReference<>();

        // Act
        PropertyAccessor<String> result = accessor.applyTo(consumed::set);

        // Assert
        assertThat(result).isSameAs(accessor);
        assertThat(consumed.get()).isEqualTo(value);
    }

    @Test
    void testApplyToWithEmptyAccessor() {
        // Arrange
        PropertyAccessor<String> accessor = PropertyAccessor.empty();
        AtomicBoolean consumerCalled = new AtomicBoolean(false);

        // Act
        PropertyAccessor<String> result = accessor.applyTo(s -> consumerCalled.set(true));

        // Assert
        assertThat(result).isSameAs(accessor);
        assertThat(consumerCalled.get()).isFalse();
    }

    @Test
    void testOrWithNonNullValueOnNonEmptyAccessor() {
        // Arrange
        String value = "test";
        PropertyAccessor<String> accessor = PropertyAccessor.of(value);

        // Act
        PropertyAccessor<String> result = accessor.or("fallback");

        // Assert
        assertThat(result).isSameAs(accessor);
        assertThat(result.get()).isEqualTo(value);
    }

    @Test
    void testOrWithNonNullValueOnEmptyAccessor() {
        // Arrange
        PropertyAccessor<String> accessor = PropertyAccessor.empty();
        String fallback = "fallback";

        // Act
        PropertyAccessor<String> result = accessor.or(fallback);

        // Assert
        assertThat(result).isInstanceOf(SimplePropertyAccessor.class);
        assertThat(result.get()).isEqualTo(fallback);
    }

    @Test
    void testOrWithNullValueOnEmptyAccessor() {
        // Arrange
        PropertyAccessor<String> accessor = PropertyAccessor.empty();

        // Act
        PropertyAccessor<String> result = accessor.or(null);

        // Assert
        assertThat(result).isSameAs(accessor);
        assertThat(result.get()).isNull();
    }

    @Test
    void testOrElseWithNonNullValueOnNonEmptyAccessor() {
        // Arrange
        String value = "test";
        PropertyAccessor<String> accessor = PropertyAccessor.of(value);
        AtomicBoolean supplierCalled = new AtomicBoolean(false);

        // Act
        PropertyAccessor<String> result = accessor.orElse(() -> {
            supplierCalled.set(true);
            return "fallback";
        });

        // Assert
        assertThat(result).isSameAs(accessor);
        assertThat(result.get()).isEqualTo(value);
        assertThat(supplierCalled.get()).isFalse();
    }

    @Test
    void testOrElseWithNonNullValueOnEmptyAccessor() {
        // Arrange
        PropertyAccessor<String> accessor = PropertyAccessor.empty();
        String fallback = "fallback";

        // Act
        PropertyAccessor<String> result = accessor.orElse(() -> fallback);

        // Assert
        assertThat(result).isInstanceOf(SimplePropertyAccessor.class);
        assertThat(result.get()).isEqualTo(fallback);
    }

    @Test
    void testOrElseWithNullValueOnEmptyAccessor() {
        // Arrange
        PropertyAccessor<String> accessor = PropertyAccessor.empty();

        // Act
        PropertyAccessor<String> result = accessor.orElse(() -> null);

        // Assert
        assertThat(result).isSameAs(accessor);
        assertThat(result.get()).isNull();
    }

    @Test
    void testChaining() {
        // Arrange
        List<String> results = new ArrayList<>();

        // Act
        String result = PropertyAccessor.of("test")
                .map(String::toUpperCase)
                .applyTo(results::add)
                .map(s -> s + "!")
                .applyTo(results::add)
                .get();

        // Assert
        assertThat(result).isEqualTo("TEST!");
        assertThat(results).containsExactly("TEST", "TEST!");
    }

    @Test
    void testChainingWithEmptyResult() {
        // Arrange
        final AtomicReference<String> captured = new AtomicReference<>();

        // Act
        String result = PropertyAccessor.of("test")
                .map(s -> (String)null)
                .or("fallback")
                .applyTo(s -> captured.set((String)s))
                .get();

        // Assert
        assertThat(result).isEqualTo("fallback");
        assertThat(captured.get()).isEqualTo("fallback");
    }

    @Test
    void testPropertyContainerUsage() {
        // Arrange
        TestContainer container = new TestContainer();

        // Act & Assert
        assertThat(container.processNonNull("test")).isEqualTo("TEST");
        assertThat(container.processNullable(null)).isEqualTo("DEFAULT");
        assertThat(container.processNullable("value")).isEqualTo("VALUE");
    }

    /**
     * Simple test implementation of PropertyContainer for testing purposes.
     */
    private static class TestContainer extends PropertyContainer {
        
        public String processNonNull(String value) {
            return property(value)
                    .map(String::toUpperCase)
                    .get();
        }
        
        public String processNullable(String value) {
            return property(value)
                    .or("default")
                    .map(String::toUpperCase)
                    .get();
        }
    }
}