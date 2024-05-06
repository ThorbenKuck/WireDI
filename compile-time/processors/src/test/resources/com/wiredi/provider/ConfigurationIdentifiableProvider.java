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
public final class ConfigurationIdentifiableProvider implements IdentifiableProvider<Configuration> {
    private static final TypeIdentifier<Configuration> PRIMARY_WIRE_TYPE = TypeIdentifier.of(Configuration.class);

    private final Value<Configuration> instance = Value.empty();

    private Configuration createInstance(final WireRepository wireRepository,
                                         final TypeIdentifier<Configuration> concreteType) {
        Configuration instance = new Configuration();
        return instance;
    }

    @Override
    public final synchronized Configuration get(@NotNull final WireRepository wireRepository,
                                                @NotNull final TypeIdentifier<Configuration> concreteType) {
        return instance.getOrSet(() -> createInstance(wireRepository, concreteType));
    }

    @Override
    @NotNull
    public final TypeIdentifier<Configuration> type() {
        return PRIMARY_WIRE_TYPE;
    }
}
