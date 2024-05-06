package com.wiredi.runtime.environment.resolvers;

import com.google.auto.service.AutoService;
import com.wiredi.runtime.Environment;
import com.wiredi.runtime.environment.Placeholder;
import com.wiredi.runtime.properties.Key;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

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
