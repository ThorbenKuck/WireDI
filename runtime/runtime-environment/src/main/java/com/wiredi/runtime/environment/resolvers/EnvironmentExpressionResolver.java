package com.wiredi.runtime.environment.resolvers;

import com.wiredi.runtime.domain.Ordered;
import com.wiredi.runtime.Environment;
import com.wiredi.runtime.environment.Placeholder;

import java.util.Optional;

/**
 * A EnvironmentExpressionResolver is a class that allows for custom property placeholder prefixes.
 * <p>
 * For example: To implement support for the prefix /, which would look like this: "/{...}", you would need to provide
 * a custom EnvironmentExpressionResolver, which handles the identifier "/".
 * <p>
 * Please note: This class should be stateless. It is loaded once globally and hence any state will stay for multiple
 * executions of different WireRepositories.
 */
public interface EnvironmentExpressionResolver extends Ordered {

    Optional<String> resolve(Placeholder placeholder, Environment environment);

    char expressionIdentifier();

}
