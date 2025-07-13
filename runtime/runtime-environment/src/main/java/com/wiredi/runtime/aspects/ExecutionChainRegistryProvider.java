package com.wiredi.runtime.aspects;

import com.google.auto.service.AutoService;
import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.values.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoService(IdentifiableProvider.class)
public class ExecutionChainRegistryProvider implements IdentifiableProvider<ExecutionChainRegistry> {

    private final Value<ExecutionChainRegistry> instance = Value.empty();

    @Override
    public @NotNull TypeIdentifier<? super ExecutionChainRegistry> type() {
        return TypeIdentifier.of(ExecutionChainRegistry.class);
    }

    @Override
    public @Nullable ExecutionChainRegistry get(@NotNull WireContainer wireContainer, @NotNull TypeIdentifier<ExecutionChainRegistry> concreteType) {
        return instance.getOrSet(() -> new ExecutionChainRegistry(wireContainer.getAll(AspectHandler.class)));
    }
}
