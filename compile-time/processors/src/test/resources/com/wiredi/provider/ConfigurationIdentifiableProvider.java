package com.wiredi.provider;

import com.google.auto.service.AutoService;
import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import jakarta.annotation.Generated;
import java.lang.Override;
import org.jetbrains.annotations.NotNull;

@Generated(
        value = "com.wiredi.compiler.domain.entities.IdentifiableProviderEntity",
        date = "2023-01-01T00:00Z"
)
@AutoService({IdentifiableProvider.class})
public final class ConfigurationIdentifiableProvider implements IdentifiableProvider<Configuration> {
    private static final TypeIdentifier<Configuration> PRIMARY_WIRE_TYPE = TypeIdentifier.of(Configuration.class);

    private Configuration createInstance(final WireContainer wireContainer,
                                         final TypeIdentifier<Configuration> concreteType) {
        Configuration instance = new Configuration();
        return instance;
    }

    @Override
    public final Configuration get(@NotNull final WireContainer wireContainer,
                                   @NotNull final TypeIdentifier<Configuration> concreteType) {
        return createInstance(wireContainer, concreteType);
    }

    @Override
    @NotNull
    public final TypeIdentifier<Configuration> type() {
        return PRIMARY_WIRE_TYPE;
    }
}
