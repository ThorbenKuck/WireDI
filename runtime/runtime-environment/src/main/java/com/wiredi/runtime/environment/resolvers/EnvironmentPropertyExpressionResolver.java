package com.wiredi.runtime.environment.resolvers;

import com.google.auto.service.AutoService;
import com.wiredi.runtime.Environment;
import com.wiredi.runtime.environment.Placeholder;
import com.wiredi.runtime.properties.Key;

import java.util.Optional;

@AutoService(EnvironmentExpressionResolver.class)
public class EnvironmentPropertyExpressionResolver implements EnvironmentExpressionResolver {
    @Override
    public Optional<String> resolve(Placeholder placeholder, Environment environment) {
        return Optional.ofNullable(environment.getProperty(Key.format(placeholder.getExpression())))
                .or(() -> propertyFromParameters(placeholder));
    }

    private Optional<String> propertyFromParameters(Placeholder placeholder) {
        for (Placeholder.Parameter parameter : placeholder.getParameters()) {
            Optional<Placeholder> resolvedParameter = parameter.asPlaceholder();
            if (resolvedParameter.isPresent()) {
                return resolvedParameter.map(Placeholder::compile);
            }
        }

        return Optional.empty();
    }

    @Override
    public char expressionIdentifier() {
        return '$';
    }
}
