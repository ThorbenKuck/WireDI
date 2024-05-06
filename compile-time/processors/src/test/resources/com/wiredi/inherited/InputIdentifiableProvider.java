package com.wiredi.inherited;

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
public final class InputIdentifiableProvider implements IdentifiableProvider<Input> {
    private static final TypeIdentifier<Input> PRIMARY_WIRE_TYPE = TypeIdentifier.of(Input.class);

    private final Value<Input> instance = Value.empty();

    private Input createInstance(final WireRepository wireRepository,
                                 final TypeIdentifier<Input> concreteType) {
        Input instance = new Input();
        return instance;
    }

    @Override
    public final int getOrder() {
        return 101;
    }

    @Override
    public final synchronized Input get(@NotNull final WireRepository wireRepository,
                                        @NotNull final TypeIdentifier<Input> concreteType) {
        return instance.getOrSet(() -> createInstance(wireRepository, concreteType));
    }

    @Override
    @NotNull
    public final TypeIdentifier<Input> type() {
        return PRIMARY_WIRE_TYPE;
    }
}
