package com.wiredi.aop;

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
public final class TransactionalHandlerIdentifiableProvider implements IdentifiableProvider<TransactionalHandler> {
    private static final TypeIdentifier<TransactionalHandler> PRIMARY_WIRE_TYPE = TypeIdentifier.of(TransactionalHandler.class);

    private final Value<TransactionalHandler> instance = Value.empty();

    private TransactionalHandler createInstance(final WireRepository wireRepository,
                                                final TypeIdentifier<TransactionalHandler> concreteType) {
        TransactionalHandler instance = new TransactionalHandler();
        return instance;
    }

    @Override
    public final synchronized TransactionalHandler get(@NotNull final WireRepository wireRepository,
                                                       @NotNull final TypeIdentifier<TransactionalHandler> concreteType) {
        return instance.getOrSet(() -> createInstance(wireRepository, concreteType));
    }

    @Override
    @NotNull
    public final TypeIdentifier<TransactionalHandler> type() {
        return PRIMARY_WIRE_TYPE;
    }
}
