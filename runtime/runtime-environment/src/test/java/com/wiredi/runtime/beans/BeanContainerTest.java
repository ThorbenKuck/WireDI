package com.wiredi.runtime.beans;

import com.wiredi.runtime.Environment;
import com.wiredi.runtime.OnDemandInjector;
import com.wiredi.runtime.ServiceLoader;
import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.AnnotationMetaData;
import com.wiredi.runtime.domain.StandardWireConflictResolver;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnBeanEvaluator;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.IdentifiableProviderSource;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.domain.provider.condition.LoadCondition;
import com.wiredi.runtime.domain.provider.sources.FixedIdentifiableProviderSource;
import com.wiredi.tests.CaptureOutput;
import com.wiredi.tests.CapturedOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// TODO: Implement and show that TypeIdentifier work as expected (i.e. search for List returns also List<String>, but not the other way around)
class BeanContainerTest {

    private static final AnnotationMetaData CONDITIONAL_ON_STRING = AnnotationMetaData.newInstance("ConditionalOnBean").withField("type", String.class).build();
    private static final AnnotationMetaData CONDITIONAL_ON_FLOAT = AnnotationMetaData.newInstance("ConditionalOnBean").withField("type", Float.class).build();
    private static final AnnotationMetaData CONDITIONAL_ON_DOUBLE = AnnotationMetaData.newInstance("ConditionalOnBean").withField("type", Double.class).build();

    @Test
    @CaptureOutput
    public void conditionRoundsAreExecutedUntilAllConditionsAreApplied(CapturedOutput capturedOutput) {
        // Arrange
        WireRepository wireRepository = Mockito.mock(WireRepository.class);
        OnDemandInjector onDemandInjector = Mockito.mock(OnDemandInjector.class);
        Mockito.when(onDemandInjector.get(ConditionalOnBeanEvaluator.class)).thenReturn(new ConditionalOnBeanEvaluator());
        Environment environment = new Environment();
        BeanContainer container = new BeanContainer(
                new BeanContainerProperties(
                        1,
                        () -> StandardWireConflictResolver.DEFAULT
                ),
                List.of(IdentifiableProviderSource.just(
                        new NeverMetCondition(),
                        new Base(),
                        new NeverMetCondition(),
                        new FirstCondition(),
                        new NeverMetCondition(),
                        new SecondCondition(),
                        new NeverMetCondition()
                ))
        );
        Mockito.when(wireRepository.environment()).thenReturn(environment);
        Mockito.when(wireRepository.beanContainer()).thenReturn(container);
        Mockito.when(wireRepository.onDemandInjector()).thenReturn(onDemandInjector);

        // Act
        container.load(wireRepository);
        assertThat(capturedOutput.getOutput()).contains("Applied 2 conditional providers in 3 rounds. Consider to optimize the condition orders to reduce the rounds required for conditional checks.");

        // Assert
        assertThat(container.size()).isEqualTo(3);
    }

    class Base implements IdentifiableProvider<String> {

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

    class FirstCondition implements IdentifiableProvider<Integer> {

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

    class SecondCondition implements IdentifiableProvider<Float> {

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

    class NeverMetCondition implements IdentifiableProvider<Integer> {

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