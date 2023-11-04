package com.wiredi.environment.resolvers;

import com.google.auto.service.AutoService;
import com.wiredi.environment.Environment;
import com.wiredi.environment.Placeholder;
import com.wiredi.environment.resolvers.EnvironmentExpressionResolver;
import com.wiredi.properties.keys.Key;

import java.util.Optional;

@AutoService(EnvironmentExpressionResolver.class)
public class EnvironmentPropertyExpressionResolver implements EnvironmentExpressionResolver {
	@Override
	public Optional<String> resolve(Placeholder placeholder, Environment environment) {
		return Optional.ofNullable(environment.getProperty(Key.format(placeholder.getContent())));
	}

	@Override
	public char expressionIdentifier() {
		return '$';
	}
}
