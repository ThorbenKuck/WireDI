package com.wiredi.runtime.properties;

import com.wiredi.runtime.properties.keys.PreFormattedKey;
import com.wiredi.runtime.time.Timed;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TypedPropertiesTest {

    @Test
    public void subsetTest() {
        // Arrange
        TypedProperties properties = new TypedProperties();
        properties.set(Key.just("com.example.test"), "test");

        // Act
        Map<String, String> subset = properties.subsetOf("com.example");

        // Assert
        assertThat(subset).hasSize(1);
        assertThat(subset.get("test")).isEqualTo("test");
    }

    @Test
    public void subsetTestDeep() {
        // Arrange
        TypedProperties properties = new TypedProperties();
        properties.set(Key.just("com.example.foo.bar"), "baz");

        // Act
        Map<String, String> subset = properties.subsetOf("com.example");

        // Assert
        assertThat(subset).hasSize(1);
        assertThat(subset.get("foo.bar")).isEqualTo("baz");
    }

    @RepeatedTest(50)
    public void performanceTest() {
        Key test = Key.just("test");
        try (TypedProperties properties = new TypedProperties().set(test, "test")) {
            long repetitions = 1000000;

            Timed environmentUnawareResult = Timed.of(() -> {
                for (int i = 0; i < repetitions; i++) {
                    properties.get(test);
                }
            });

            System.out.println("Unaware: " + environmentUnawareResult);
        }
    }

    @ParameterizedTest
    @MethodSource("enumValues")
    public void enumsCanBeDetermined(String rawName, EnumValues expected) {
        PreFormattedKey key = Key.just("key");
        try (TypedProperties typedProperties = new TypedProperties()) {
            typedProperties.set(key, rawName);
            Optional<EnumValues> value = typedProperties.tryGet(key, EnumValues.class);
            assertThat(value).isPresent().contains(expected);
        }
    }

    public static Stream<Arguments> enumValues() {
        return Stream.of(
                Arguments.of("more-complex-name", EnumValues.MORE_COMPLEX_NAME),
                Arguments.of("more_complex_name", EnumValues.MORE_COMPLEX_NAME),
                Arguments.of("MORE_complex_NAME", EnumValues.MORE_COMPLEX_NAME),
                Arguments.of("MoRe_CoMpLeX_nAmE", EnumValues.MORE_COMPLEX_NAME),
                Arguments.of("MoRe-CoMpLeX-nAmE", EnumValues.MORE_COMPLEX_NAME),
                Arguments.of("one", EnumValues.ONE),
                Arguments.of("oNe", EnumValues.ONE),
                Arguments.of("oNE", EnumValues.ONE),
                Arguments.of("One", EnumValues.ONE),
                Arguments.of("OnE", EnumValues.ONE),
                Arguments.of("ONE", EnumValues.ONE)
        );
    }
}