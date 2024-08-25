package com.wiredi.runtime.environment.builtin;

import com.wiredi.runtime.Environment;
import com.wiredi.runtime.environment.EnvironmentConfiguration;
import com.wiredi.runtime.types.TypeConverter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TypedPropertiesEnvironmentConfiguration implements EnvironmentConfiguration {

    private final List<TypeConverter<?>> converters;

    public TypedPropertiesEnvironmentConfiguration(List<TypeConverter<?>> converters) {
        this.converters = converters;
    }

    @Override
    public void configure(@NotNull Environment environment) {
        converters.forEach(converter -> environment.typeMapper()
                .setTypeConverter(converter)
                .forAllSupportedTypes()
        );
    }
}
