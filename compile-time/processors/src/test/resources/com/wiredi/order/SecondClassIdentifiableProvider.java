package com.wiredi.order.input;

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
public final class SecondClassIdentifiableProvider implements IdentifiableProvider<SecondClass> {
    private static final TypeIdentifier<SecondClass> PRIMARY_WIRE_TYPE = TypeIdentifier.of(SecondClass.class);

    private final Value<SecondClass> instance = Value.empty();

    private SecondClass createInstance(final WireRepository wireRepository,
                                       final TypeIdentifier<SecondClass> concreteType) {
        SecondClass instance = new SecondClass();
        return instance;
    }

    @Override
    public final int getOrder() {
        return -6;
    }

    @Override
    public final synchronized SecondClass get(@NotNull final WireRepository wireRepository,
                                              @NotNull final TypeIdentifier<SecondClass> concreteType) {
        return instance.getOrSet(() -> createInstance(wireRepository, concreteType));
    }

    @Override
    @NotNull
    public final TypeIdentifier<SecondClass> type() {
        return PRIMARY_WIRE_TYPE;
    }
}
