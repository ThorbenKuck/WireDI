package com.wiredi.environment.resolvers;

import com.google.auto.service.AutoService;
import com.wiredi.environment.Environment;
import com.wiredi.environment.resolvers.EnvironmentExpressionResolver;
import com.wiredi.properties.keys.Key;

import java.util.Optional;

@AutoService(EnvironmentExpressionResolver.class)
public class EnvironmentPropertyExpressionResolver implements EnvironmentExpressionResolver {
	@Override
	public Optional<String> resolve(Key key, Environment environment) {
		return Optional.ofNullable(environment.getProperty(key));
	}

	@Override
	public char identifier() {
		return '$';
	}
}
