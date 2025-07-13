package com.wiredi.runtime;

import com.wiredi.runtime.domain.annotations.AnnotationMetadata;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnBeanEvaluator;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.IdentifiableProviderSource;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.domain.provider.condition.LoadCondition;
import com.wiredi.runtime.properties.Key;
import com.wiredi.tests.CaptureOutput;
import com.wiredi.tests.CapturedOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WireBootstrapTest {

    private static final AnnotationMetadata CONDITIONAL_ON_STRING = AnnotationMetadata.builder("ConditionalOnBean").withField("type", String.class).build();
    private static final AnnotationMetadata CONDITIONAL_ON_FLOAT = AnnotationMetadata.builder("ConditionalOnBean").withField("type", Float.class).build();
    private static final AnnotationMetadata CONDITIONAL_ON_DOUBLE = AnnotationMetadata.builder("ConditionalOnBean").withField("type", Double.class).build();

    @Test
    @CaptureOutput
    public void conditionRoundsAreExecutedUntilAllConditionsAreApplied(CapturedOutput capturedOutput) {
        // Arrange
        Environment environment = new Environment();
        environment.setProperty(PropertyKeys.CONDITIONAL_ROUND_THRESHOLD.getKey(), "1");
        environment.setProperty(Key.just("debug"), "true");

        WireContainer wireContainer = new WireContainer(environment);
        wireContainer.initializer().setSources(List.of(IdentifiableProviderSource.just(
                new NeverMetCondition(),
                new Base(),
                new NeverMetCondition(),
                new FirstCondition(),
                new NeverMetCondition(),
                new SecondCondition(),
                new NeverMetCondition()
        )));

        // Act
        wireContainer.load();

        // Assert
        assertThat(wireContainer.isLoaded()).isTrue();
        assertThat(wireContainer.tryGet(TypeIdentifier.INTEGER)).isNotEmpty();
        assertThat(capturedOutput.getOutput()).contains("Applied 2 conditional providers in 3 rounds. Consider to optimize the condition orders to reduce the rounds required for conditional checks.");
    }

    static class Base implements IdentifiableProvider<String> {

        @Override
        public @NotNull TypeIdentifier<? super String> type() {
            return TypeIdentifier.STRING;
        }

        @Override
        public @Nullable String get(@NotNull WireContainer wireContainer, @NotNull TypeIdentifier<String> concreteType) {
            return "Test";
        }

        @Override
        public int getOrder() {
            return 3;
        }
    }

    static class FirstCondition implements IdentifiableProvider<Integer> {

        @Override
        public @NotNull TypeIdentifier<? super Integer> type() {
            return TypeIdentifier.INTEGER;
        }

        @Override
        public @Nullable Integer get(@NotNull WireContainer wireContainer, @NotNull TypeIdentifier<Integer> concreteType) {
            return 1;
        }

        @Override
        public int getOrder() {
            return 1;
        }

        @Override
        public @Nullable LoadCondition condition() {
            return LoadCondition.just(ConditionalOnBeanEvaluator.class, CONDITIONAL_ON_STRING)
                    .and(
                            LoadCondition.just(ConditionalOnBeanEvaluator.class, CONDITIONAL_ON_FLOAT)
                    );
        }
    }

    static class SecondCondition implements IdentifiableProvider<Float> {

        @Override
        public @NotNull TypeIdentifier<? super Float> type() {
            return TypeIdentifier.FLOAT;
        }

        @Override
        public @Nullable Float get(@NotNull WireContainer wireContainer, @NotNull TypeIdentifier<Float> concreteType) {
            return 0.1f;
        }

        @Override
        public int getOrder() {
            return 2;
        }

        @Override
        public @Nullable LoadCondition condition() {
            return LoadCondition.just(ConditionalOnBeanEvaluator.class, CONDITIONAL_ON_STRING);
        }
    }

    static class NeverMetCondition implements IdentifiableProvider<Integer> {

        @Override
        public @NotNull TypeIdentifier<? super Integer> type() {
            return TypeIdentifier.INTEGER;
        }

        @Override
        public @Nullable Integer get(@NotNull WireContainer wireContainer, @NotNull TypeIdentifier<Integer> concreteType) {
            return 0;
        }

        @Override
        public int getOrder() {
            return -1;
        }

        @Override
        public @Nullable LoadCondition condition() {
            return LoadCondition.just(ConditionalOnBeanEvaluator.class, CONDITIONAL_ON_DOUBLE);
        }
    }

}
