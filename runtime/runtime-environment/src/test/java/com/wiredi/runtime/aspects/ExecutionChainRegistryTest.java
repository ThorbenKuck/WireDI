package com.wiredi.runtime.aspects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ExecutionChainRegistryTest {

    private final RootMethod FIRST_METHOD = RootMethod.newInstance("first").build(it -> it);
    private final RootMethod SECOND_METHOD = RootMethod.newInstance("second").build(it -> it);

    @Test
    public void addingHandlerUpdatesRelevantChains() {
        // Arrange
        final ExecutionChainRegistry registry = new ExecutionChainRegistry();
        ExecutionChain firstChain = registry.getExecutionChain(FIRST_METHOD);
        ExecutionChain secondChain = registry.getExecutionChain(SECOND_METHOD);
        AspectHandler firstHandler = new AspectHandler() {
            @Override
            public @Nullable Object process(@NotNull ExecutionContext context) {
                return context.proceed();
            }

            @Override
            public boolean appliesTo(@NotNull RootMethod rootMethod) {
                return rootMethod.equals(FIRST_METHOD);
            }
        };
        AspectHandler secondHandler = new AspectHandler() {
            @Override
            public @Nullable Object process(@NotNull ExecutionContext context) {
                return context.proceed();
            }

            @Override
            public boolean appliesTo(@NotNull RootMethod rootMethod) {
                return rootMethod.equals(SECOND_METHOD);
            }
        };

        // Act
        registry.setAspectHandlers(List.of(firstHandler, secondHandler));

        // Assert
        assertThat(firstChain.head()).isSameAs(firstChain.tail()).isSameAs(firstHandler);
        assertThat(secondChain.head()).isSameAs(secondChain.tail()).isSameAs(secondHandler);
    }

}
