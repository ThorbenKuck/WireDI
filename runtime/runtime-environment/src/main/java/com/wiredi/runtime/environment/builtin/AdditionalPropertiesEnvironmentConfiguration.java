package com.wiredi.runtime.environment.builtin;

import com.google.auto.service.AutoService;
import com.wiredi.runtime.domain.Ordered;
import com.wiredi.runtime.Environment;
import com.wiredi.runtime.resources.Resource;
import com.wiredi.runtime.environment.EnvironmentConfiguration;
import org.jetbrains.annotations.NotNull;

import static com.wiredi.runtime.environment.DefaultEnvironmentKeys.ADDITIONAL_PROPERTIES;

@AutoService(EnvironmentConfiguration.class)
public class AdditionalPropertiesEnvironmentConfiguration implements EnvironmentConfiguration {

	public static final int ORDER = Ordered.after(ApplicationPropertiesEnvironmentConfiguration.ORDER);

	@Override
	public void configure(@NotNull Environment environment) {
		environment.getAllProperties(ADDITIONAL_PROPERTIES)
				.parallelStream()
				.map(environment::loadResource)
				.filter(Resource::exists)
				.filter(Resource::isFile)
				.forEach(environment::appendPropertiesFrom);
	}


	@Override
	public int getOrder() {
		return ORDER;
	}
}
