package com.wiredi.runtime.converter;

import com.google.auto.service.AutoService;
import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.types.TypeConverter;
import com.wiredi.runtime.values.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;

@AutoService(IdentifiableProvider.class)
public class InstantTypeConverterProvider implements IdentifiableProvider<InstantTypeConverter> {

    private final Value<InstantTypeConverter> value = Value.empty();

    @Override
    public @NotNull TypeIdentifier<? super InstantTypeConverter> type() {
        return TypeIdentifier.just(InstantTypeConverter.class);
    }

    @Override
    public @NotNull List<TypeIdentifier<?>> additionalWireTypes() {
        return List.of(
                TypeIdentifier.of(TypeConverter.class).withGeneric(Instant.class)
        );
    }

    @Override
    public @Nullable InstantTypeConverter get(
            @NotNull WireContainer wireRepository,
            @NotNull TypeIdentifier<InstantTypeConverter> concreteType
    ) {
        return value.getOrSet(() -> new InstantTypeConverter());
    }
}
