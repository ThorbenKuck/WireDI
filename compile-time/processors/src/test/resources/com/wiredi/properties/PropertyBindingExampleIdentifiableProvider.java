package com.wiredi.properties;

import com.google.auto.service.AutoService;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.lang.DynamicBuilder;
import com.wiredi.runtime.properties.TypedProperties;
import com.wiredi.runtime.properties.Key;
import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.values.Value;
import jakarta.annotation.Generated;
import java.lang.Override;
import org.jetbrains.annotations.NotNull;

@Generated(
        value = "com.wiredi.compiler.domain.entities.IdentifiableProviderEntity",
        date = "2023-01-01T00:00Z"
)
@AutoService({IdentifiableProvider.class})
public final class PropertyBindingExampleIdentifiableProvider implements IdentifiableProvider<PropertyBindingExample> {
    private static final TypeIdentifier<PropertyBindingExample> PRIMARY_WIRE_TYPE = TypeIdentifier.of(PropertyBindingExample.class);

    private final Value<PropertyBindingExample> instance = Value.empty();

    private PropertyBindingExample createInstance(final WireRepository wireRepository) {
        TypedProperties properties = wireRepository.environment().properties();
        return DynamicBuilder.of(
                    new PropertyBindingExample(
                        properties.require(Key.just("test.properties.string")),
                        properties.require(Key.just("test.properties.integer"), int.class),
                        new PropertyBindingExample.Nested(
                            properties.require(Key.just("test.properties.nested.string")),
                            properties.require(Key.just("test.properties.nested.integer"), int.class)
                        ),
                        properties.require(Key.just("test.properties.enum-value"), PropertyBindingEnum.class)
                    )
                ).setup(instance -> {
                    properties.get(Key.just("set-setter-enum-value"), PropertyBindingEnum.class).ifPresent(instance::setSetterEnumValue);
                });
    }

    @Override
    public final synchronized PropertyBindingExample get(
            @NotNull final WireRepository wireRepository) {
        return instance.getOrSet(() -> createInstance(wireRepository));
    }

    @Override
    @NotNull
    public final TypeIdentifier<PropertyBindingExample> type() {
        return PRIMARY_WIRE_TYPE;
    }
}
