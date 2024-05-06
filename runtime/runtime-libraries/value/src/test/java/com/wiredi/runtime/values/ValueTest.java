package com.wiredi.runtime.values;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.junit.jupiter.api.Named.named;

class ValueTest {

    public static Stream<Arguments> allValues() {
        return Stream.of(
                Arguments.of(named("Value.lazy", new Creator((s) -> Value.lazy(() -> s)))),
                Arguments.of(named("Value.just", new Creator(Value::just))),
                Arguments.of(named("Value.neverNull", new Creator(Value::neverNull))),
                Arguments.of(named("Value.synchronize", new Creator(Value::synchronize))),
                Arguments.of(named("Value.async", new Creator((s) -> Value.async(() -> s))))
        );
    }

    public static Stream<Arguments> intiallyEmptyValues() {
        return Stream.of(
                Arguments.of(named("Value.empty", new EmptyCreator(Value::empty))),
                Arguments.of(named("Value.synchronize", new EmptyCreator(Value::synchronize)))
        );
    }

    @ParameterizedTest
    @MethodSource("allValues")
    public void handleParameter(Creator valueFunction) {
        // Arrange
        String initialInput = "First";
        String secondInput = "Second";
        Value<String> value = valueFunction.create(initialInput);
        assertThat(value.get()).isEqualTo(initialInput);
        assertThat(value.isSet()).isEqualTo(true);
        assertThat(value.isEmpty()).isEqualTo(false);

        // Act
        value.set(secondInput);
        assertThat(value.isSet()).isEqualTo(true);
        assertThat(value.isEmpty()).isEqualTo(false);
        value.ifPresent(it -> assertThat(it).isEqualTo(secondInput))
                .orElse(() -> fail("Value was empty"));

        // Assert
        assertThat(value.get()).isEqualTo(secondInput);
        value.ifEmpty(() -> fail("Value was empty"));
    }

    @ParameterizedTest
    @MethodSource("intiallyEmptyValues")
    public void emptyConstructorInvocations(EmptyCreator valueFunction) {
        // Arrange
        String secondInput = "Second";
        Value<String> value = valueFunction.create();
        assertThat(value.isSet()).isEqualTo(false);
        assertThat(value.isEmpty()).isEqualTo(true);
        value.ifEmpty(() -> {});
        value.ifPresent((s) -> fail("Should have been empty"))
                .orElse(() -> {});

        // Act
        value.set(secondInput);
        assertThat(value.isSet()).isEqualTo(true);
        assertThat(value.isEmpty()).isEqualTo(false);
        value.ifPresent(it -> assertThat(it).isEqualTo(secondInput))
                .orElse(() -> fail("Value was empty"));

        // Assert
        assertThat(value.get()).isEqualTo(secondInput);
        value.ifEmpty(() -> fail("Value was empty"));
    }

    static class Creator {

        private final Function<String, Value<String>> delegate;

        Creator(Function<String, Value<String>> delegate) {
            this.delegate = delegate;
        }

        public Value<String> create(String s) {
            return delegate.apply(s);
        }
    }

    static class EmptyCreator {

        private final Supplier<Value<String>> delegate;

        EmptyCreator(Supplier<Value<String>> delegate) {
            this.delegate = delegate;
        }

        public Value<String> create() {
            return delegate.get();
        }
    }
}