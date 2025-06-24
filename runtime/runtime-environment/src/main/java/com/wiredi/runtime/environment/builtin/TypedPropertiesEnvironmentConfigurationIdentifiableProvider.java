package com.wiredi.runtime.environment.builtin;

import com.google.auto.service.AutoService;
import com.wiredi.annotations.Order;
import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.annotations.AnnotationMetadata;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnPropertyEvaluator;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.domain.provider.condition.EagerLoadCondition;
import com.wiredi.runtime.domain.provider.condition.LoadCondition;
import com.wiredi.runtime.environment.EnvironmentConfiguration;
import com.wiredi.runtime.types.TypeConverter;
import com.wiredi.runtime.values.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.wiredi.runtime.environment.DefaultEnvironmentKeys.APPLY_TYPE_CONVERTERS_VALUE;

@AutoService(IdentifiableProvider.class)
public class TypedPropertiesEnvironmentConfigurationIdentifiableProvider implements IdentifiableProvider<TypedPropertiesEnvironmentConfiguration> {

    private static final Value<LoadCondition> LOAD_CONDITION_VALUE = Value.async(() -> new EagerLoadCondition(
            new ConditionalOnPropertyEvaluator(),
            AnnotationMetadata.builder(APPLY_TYPE_CONVERTERS_VALUE)
                    .withField("key", APPLY_TYPE_CONVERTERS_VALUE)
                    .withField("havingValue", "true")
                    .withField("matchIfMissing", "true")
                    .build()
    ));
    private final Value<TypedPropertiesEnvironmentConfiguration> instance = Value.empty();

    @Override
    public @NotNull TypeIdentifier<? super TypedPropertiesEnvironmentConfiguration> type() {
        return TypeIdentifier.of(TypedPropertiesEnvironmentConfiguration.class);
    }

    @Override
    public @NotNull List<TypeIdentifier<?>> additionalWireTypes() {
        return List.of(
                TypeIdentifier.just(EnvironmentConfiguration.class)
        );
    }

    @Override
    public int getOrder() {
        return Order.FIRST;
    }

    @Override
    public @Nullable LoadCondition condition() {
        return LOAD_CONDITION_VALUE.get();
    }

    @Override
    public @Nullable TypedPropertiesEnvironmentConfiguration get(
            @NotNull WireRepository wireRepository,
            @NotNull TypeIdentifier<TypedPropertiesEnvironmentConfiguration> concreteType
    ) {
        return this.instance.getOrSet(() -> new TypedPropertiesEnvironmentConfiguration(
                        wireRepository.getAll(
                                TypeIdentifier.of(TypeConverter.class)
                                        .withWildcard()
                        )
                )
        );
    }
}
