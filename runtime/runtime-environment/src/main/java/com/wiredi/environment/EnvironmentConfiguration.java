package com.wiredi.environment;

import com.wiredi.domain.Ordered;
import com.wiredi.environment.Environment;
import org.jetbrains.annotations.NotNull;

public interface EnvironmentConfiguration extends Ordered {

	void configure(@NotNull Environment environment);

}
