package com.wiredi.inherited;

import com.google.auto.service.AutoService;
import com.wiredi.runtime.WireContainer;
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
public final class CustomAutoConfigurationIdentifiableProvider implements IdentifiableProvider<CustomAutoConfiguration> {
    private static final TypeIdentifier<CustomAutoConfiguration> PRIMARY_WIRE_TYPE = TypeIdentifier.of(CustomAutoConfiguration.class);

    private final Value<CustomAutoConfiguration> instance = Value.empty();

    private CustomAutoConfiguration createInstance(final WireContainer wireRepository,
                                                   final TypeIdentifier<CustomAutoConfiguration> concreteType) {
        CustomAutoConfiguration instance = new CustomAutoConfiguration();
        return instance;
    }

    @Override
    public final int getOrder() {
        return -100;
    }

    @Override
    public final synchronized CustomAutoConfiguration get(
            @NotNull final WireContainer wireRepository,
            @NotNull final TypeIdentifier<CustomAutoConfiguration> concreteType) {
        return instance.getOrSet(() -> createInstance(wireRepository, concreteType));
    }

    @Override
    @NotNull
    public final TypeIdentifier<CustomAutoConfiguration> type() {
        return PRIMARY_WIRE_TYPE;
    }
}
