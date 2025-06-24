package com.wiredi.runtime;

import com.wiredi.runtime.beans.BeanContainer;
import com.wiredi.runtime.beans.BeanContainerProperties;
import com.wiredi.runtime.domain.ScopeRegistry;
import com.wiredi.runtime.domain.StandardWireConflictResolver;
import com.wiredi.runtime.domain.annotations.AnnotationMetadata;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnBeanEvaluator;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.IdentifiableProviderSource;
import com.wiredi.runtime.domain.provider.QualifiedTypeIdentifier;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.domain.provider.condition.LoadCondition;
import com.wiredi.runtime.properties.Key;
import com.wiredi.tests.CaptureOutput;
import com.wiredi.tests.CapturedOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class WireBootstrapTest {

    private static final AnnotationMetadata CONDITIONAL_ON_STRING = AnnotationMetadata.builder("ConditionalOnBean").withField("type", String.class).build();
    private static final AnnotationMetadata CONDITIONAL_ON_FLOAT = AnnotationMetadata.builder("ConditionalOnBean").withField("type", Float.class).build();
    private static final AnnotationMetadata CONDITIONAL_ON_DOUBLE = AnnotationMetadata.builder("ConditionalOnBean").withField("type", Double.class).build();

    @Test
    @CaptureOutput
    public void conditionRoundsAreExecutedUntilAllConditionsAreApplied(CapturedOutput capturedOutput) {
        // Arrange
        WireRepository wireRepository = Mockito.mock(WireRepository.class);
        OnDemandInjector onDemandInjector = Mockito.mock(OnDemandInjector.class);
        ScopeRegistry scopeRegistry = new ScopeRegistry(wireRepository);
        StartupDiagnostics startupDiagnostics = new StartupDiagnostics();
        Mockito.when(onDemandInjector.get(ConditionalOnBeanEvaluator.class)).thenReturn(new ConditionalOnBeanEvaluator());
        Mockito.when(wireRepository.scopeRegistry()).thenReturn(scopeRegistry);

        Environment environment = new Environment();
        environment.setProperty(PropertyKeys.CONDITIONAL_ROUND_THRESHOLD.getKey(), "1");
        environment.setProperty(Key.just("debug"), "true");

        Mockito.when(wireRepository.startupDiagnostics()).thenReturn(startupDiagnostics);
        Mockito.when(wireRepository.environment()).thenReturn(environment);
        Mockito.when(wireRepository.onDemandInjector()).thenReturn(onDemandInjector);

        WireBootstrap wireBootstrap = new WireBootstrap(wireRepository);
        wireBootstrap.addSource(IdentifiableProviderSource.just(
                        new NeverMetCondition(),
                        new Base(),
                        new NeverMetCondition(),
                        new FirstCondition(),
                        new NeverMetCondition(),
                        new SecondCondition(),
                        new NeverMetCondition()
                )
        );

        // Act
        wireBootstrap.load();

        // Assert
        assertThat(wireBootstrap.isLoaded()).isTrue();
        assertThat(scopeRegistry.getDefaultScope().tryGet(QualifiedTypeIdentifier.unqualified(SecondCondition.class))).isNotEmpty();
        assertThat(capturedOutput.getOutput()).contains("Applied 2 conditional providers in 2 rounds. Consider to optimize the condition orders to reduce the rounds required for conditional checks.");
    }

    static class Base implements IdentifiableProvider<String> {

        @Override
        public @NotNull TypeIdentifier<? super String> type() {
            return TypeIdentifier.STRING;
        }

        @Override
        public @Nullable String get(@NotNull WireRepository wireRepository, @NotNull TypeIdentifier<String> concreteType) {
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
        public @Nullable Integer get(@NotNull WireRepository wireRepository, @NotNull TypeIdentifier<Integer> concreteType) {
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
        public @Nullable Float get(@NotNull WireRepository wireRepository, @NotNull TypeIdentifier<Float> concreteType) {
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
        public @Nullable Integer get(@NotNull WireRepository wireRepository, @NotNull TypeIdentifier<Integer> concreteType) {
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