package com.wiredi.runtime.environment.resolvers;

import com.wiredi.runtime.lang.Ordered;
import com.wiredi.runtime.Environment;
import com.wiredi.runtime.environment.Placeholder;

import java.util.Optional;

/**
 * A EnvironmentExpressionResolver is a class that allows for custom property placeholder prefixes.
 * <p>
 * For example, To implement support for the prefix /, which would look like this: "/{...}", you'd need to provide
 * a custom EnvironmentExpressionResolver, which handles the identifier "/".
 * <p>
 * Please note: This class should be stateless. It is loaded once globally and hence any state will stay for multiple
 * executions of different WireRepositories.
 */
public interface EnvironmentExpressionResolver extends Ordered {

    /**
     * Resolves a placeholder using this resolver.
     * <p>
     * This method is called when a placeholder with this resolver's expression identifier
     * is found in a string being resolved by the environment.
     *
     * @param placeholder the placeholder to resolve
     * @param environment the environment context for resolution
     * @return an Optional containing the resolved value, or empty if the placeholder couldn't be resolved
     */
    Optional<String> resolve(Placeholder placeholder, Environment environment);

    /**
     * Returns the character that identifies expressions handled by this resolver.
     * <p>
     * For example, if this method returns '$', then this resolver will handle
     * expressions like "${property.name}".
     *
     * @return the identifier character for this resolver
     */
    char expressionIdentifier();

}
