package com.wiredi.runtime.environment;

import com.wiredi.runtime.Environment;
import com.wiredi.runtime.domain.Ordered;
import org.jetbrains.annotations.NotNull;

/**
 * A custom configuration for an Environment.
 * <p>
 * Please note: Any implementation of this class should be stateless.
 * It is loaded once globally and hence any state will stay for multiple instances of different WireRepositories.
 */
public interface EnvironmentConfiguration extends Ordered {

    void configure(@NotNull Environment environment);

}
