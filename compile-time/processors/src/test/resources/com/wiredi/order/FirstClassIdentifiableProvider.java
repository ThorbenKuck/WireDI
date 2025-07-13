package com.wiredi.order.input;

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
public final class FirstClassIdentifiableProvider implements IdentifiableProvider<FirstClass> {
    private static final TypeIdentifier<FirstClass> PRIMARY_WIRE_TYPE = TypeIdentifier.of(FirstClass.class);

    private FirstClass createInstance(final WireContainer wireRepository,
                                      final TypeIdentifier<FirstClass> concreteType) {
        FirstClass instance = new FirstClass();
        return instance;
    }

    @Override
    public final int getOrder() {
        return -5;
    }

    @Override
    public final FirstClass get(@NotNull final WireContainer wireRepository,
                                @NotNull final TypeIdentifier<FirstClass> concreteType) {
        return createInstance(wireRepository, concreteType);
    }

    @Override
    @NotNull
    public final TypeIdentifier<FirstClass> type() {
        return PRIMARY_WIRE_TYPE;
    }
}
