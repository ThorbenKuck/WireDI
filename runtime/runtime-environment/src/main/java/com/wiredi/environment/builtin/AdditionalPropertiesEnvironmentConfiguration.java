package com.wiredi.environment.builtin;

import com.google.auto.service.AutoService;
import com.wiredi.domain.Ordered;
import com.wiredi.environment.Environment;
import com.wiredi.resources.Resource;
import com.wiredi.resources.builtin.ClassPathResource;
import com.wiredi.environment.EnvironmentConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

import static com.wiredi.environment.DefaultEnvironmentKeys.ACTIVE_PROFILES;
import static com.wiredi.environment.DefaultEnvironmentKeys.ADDITIONAL_PROPERTIES;

@AutoService(EnvironmentConfiguration.class)
public class AdditionalPropertiesEnvironmentConfiguration implements EnvironmentConfiguration {

	public static final int ORDER = Ordered.after(ApplicationPropertiesEnvironmentConfiguration.ORDER);

	@Override
	public void configure(@NotNull Environment environment) {
		List<String> activeProfiles = environment.properties()
				.getAll(ACTIVE_PROFILES);

		Stream<ClassPathResource> profileProperties = activeProfiles
				.parallelStream()
				.map(profile -> "application-" + profile + ".properties")
				.map(ClassPathResource::new)
				.filter(ClassPathResource::exists)
				.filter(ClassPathResource::isFile);

		Stream<Resource> additionalProperties = environment.getAllProperties(ADDITIONAL_PROPERTIES)
				.parallelStream()
				.map(environment::loadResource)
				.filter(Resource::exists)
				.filter(Resource::isFile);

		Stream.concat(profileProperties, additionalProperties)
				.forEach(environment::appendPropertiesFrom);
	}


	@Override
	public int getOrder() {
		return ORDER;
	}
}
