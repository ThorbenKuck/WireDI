package com.wiredi.runtime.aspects;

import com.google.auto.service.AutoService;
import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoService(IdentifiableProvider.class)
public class ExecutionChainRegistryProvider implements IdentifiableProvider<ExecutionChainRegistry> {
    @Override
    public @NotNull TypeIdentifier<? super ExecutionChainRegistry> type() {
        return TypeIdentifier.of(ExecutionChainRegistry.class);
    }

    @Override
    public @Nullable ExecutionChainRegistry get(@NotNull WireRepository wireRepository, @NotNull TypeIdentifier<ExecutionChainRegistry> concreteType) {
        return ExecutionChainRegistry.getInstance();
    }
}
