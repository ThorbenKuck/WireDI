package com.wiredi.aop;

import com.google.auto.service.AutoService;
import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.aspects.AspectHandler;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.values.Value;
import jakarta.annotation.Generated;
import java.lang.Override;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@Generated(
        value = "com.wiredi.compiler.domain.entities.IdentifiableProviderEntity",
        date = "2023-01-01T00:00Z"
)
@AutoService({IdentifiableProvider.class})
public final class TransactionalHandler$handle$AspectHandlerIdentifiableProvider implements IdentifiableProvider<TransactionalHandler$handle$AspectHandler> {
    private static final TypeIdentifier<TransactionalHandler$handle$AspectHandler> PRIMARY_WIRE_TYPE = TypeIdentifier.of(TransactionalHandler$handle$AspectHandler.class);

    private static final List<TypeIdentifier<?>> ADDITIONAL_WIRE_TYPES = List.of(
            TypeIdentifier.of(AspectHandler.class)
    );

    private final Value<TransactionalHandler$handle$AspectHandler> instance = Value.empty();

    private TransactionalHandler$handle$AspectHandler createInstance(
            final WireRepository wireRepository,
            final TypeIdentifier<TransactionalHandler$handle$AspectHandler> concreteType) {
// We will start by Fetching all 1 constructor parameters
        TransactionalHandler variable0 = wireRepository.get(TypeIdentifier.of(TransactionalHandler.class));
        TransactionalHandler$handle$AspectHandler instance = new TransactionalHandler$handle$AspectHandler(variable0);
        return instance;
    }

    @Override
    @NotNull
    public final List<TypeIdentifier<?>> additionalWireTypes() {
        return ADDITIONAL_WIRE_TYPES;
    }

    @Override
    public final synchronized TransactionalHandler$handle$AspectHandler get(
            @NotNull final WireRepository wireRepository,
            @NotNull final TypeIdentifier<TransactionalHandler$handle$AspectHandler> concreteType) {
        return instance.getOrSet(() -> createInstance(wireRepository, concreteType));
    }

    @Override
    @NotNull
    public final TypeIdentifier<TransactionalHandler$handle$AspectHandler> type() {
        return PRIMARY_WIRE_TYPE;
    }
}
