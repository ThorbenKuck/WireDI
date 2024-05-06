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
public final class ProxyTarget$$AspectAwareProxyIdentifiableProvider implements IdentifiableProvider<ProxyTarget$$AspectAwareProxy> {
    private static final TypeIdentifier<ProxyTarget> PRIMARY_WIRE_TYPE = TypeIdentifier.of(ProxyTarget.class);

    private static final List<TypeIdentifier<?>> ADDITIONAL_WIRE_TYPES = List.of(
            TypeIdentifier.of(Interface.class)
    );

    private final Value<ProxyTarget$$AspectAwareProxy> instance = Value.empty();

    private ProxyTarget$$AspectAwareProxy createInstance(final WireRepository wireRepository,
                                                         final TypeIdentifier<ProxyTarget$$AspectAwareProxy> concreteType) {
// We will start by Fetching all 2 constructor parameters
        List<AspectHandler> variable0 = wireRepository.getAll(TypeIdentifier.of(AspectHandler.class));
        WireRepository variable1 = wireRepository.get(TypeIdentifier.of(WireRepository.class));
        ProxyTarget$$AspectAwareProxy instance = new ProxyTarget$$AspectAwareProxy(variable0,variable1);
        return instance;
    }

    @Override
    @NotNull
    public final List<TypeIdentifier<?>> additionalWireTypes() {
        return ADDITIONAL_WIRE_TYPES;
    }

    @Override
    public final synchronized ProxyTarget$$AspectAwareProxy get(
            @NotNull final WireRepository wireRepository,
            @NotNull final TypeIdentifier<ProxyTarget$$AspectAwareProxy> concreteType) {
        return instance.getOrSet(() -> createInstance(wireRepository, concreteType));
    }

    @Override
    @NotNull
    public final TypeIdentifier<ProxyTarget> type() {
        return PRIMARY_WIRE_TYPE;
    }
}
