package com.wiredi.provider;

import com.google.auto.service.AutoService;
import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.values.Value;
import jakarta.annotation.Generated;
import java.lang.Override;
import org.jetbrains.annotations.NotNull;

@Generated(
        value = "com.wiredi.compiler.domain.entities.IdentifiableProviderEntity",
        date = "2023-01-01T00:00Z"
)
@AutoService({IdentifiableProvider.class})
public final class InterfaceProvider$implementation$Configuration implements IdentifiableProvider<Interface> {
    private static final TypeIdentifier<Interface> PRIMARY_WIRE_TYPE = TypeIdentifier.of(Interface.class);

    private final Value<Interface> instance = Value.empty();

    private Interface createInstance(final WireRepository wireRepository,
            final TypeIdentifier<Interface> concreteType) {
        Configuration builder = wireRepository.get(TypeIdentifier.of(Configuration.class));
        Interface instance = builder.implementation(concreteType);
        return instance;
    }

    @Override
    public final synchronized Interface get(@NotNull final WireRepository wireRepository,
            @NotNull final TypeIdentifier<Interface> concreteType) {
        return instance.getOrSet(() -> createInstance(wireRepository, concreteType));
    }

    @Override
    @NotNull
    public final TypeIdentifier<Interface> type() {
        return PRIMARY_WIRE_TYPE;
    }
}
