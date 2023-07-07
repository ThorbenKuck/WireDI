package com.wiredi.environment.resolvers;

import com.wiredi.environment.Environment;
import com.wiredi.properties.keys.Key;

import java.util.Optional;

public interface EnvironmentExpressionResolver {

	Optional<String> resolve(Key key, Environment environment);

	char identifier();

}
