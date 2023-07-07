package com.wiredi.environment.builtin;

import com.wiredi.domain.Ordered;
import com.wiredi.environment.Environment;
import com.wiredi.properties.keys.Key;
import com.wiredi.resources.builtin.ClassPathResource;
import com.wiredi.environment.EnvironmentConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class OutsidePropertiesEnvironmentConfiguration implements EnvironmentConfiguration {

	public static final int ORDER = Ordered.after(InsidePropertiesEnvironmentConfiguration.ORDER);

	@Override
	public void configure(@NotNull Environment environment) {
		Stream<ClassPathResource> profileProperties = environment.properties()
				.getAll(Key.just("active.profiles"))
				.parallelStream()
				.map(profile -> "application-" + profile + ".properties")
				.map(ClassPathResource::new)
				.filter(ClassPathResource::exists)
				.filter(ClassPathResource::isFile);

		Stream<ClassPathResource> additionalProperties = environment.getAllProperties(Key.just("load.additional-properties"))
				.parallelStream()
				.map(ClassPathResource::new)
				.filter(ClassPathResource::exists)
				.filter(ClassPathResource::isFile);

		Stream.concat(profileProperties, additionalProperties)
				.forEach(environment::appendPropertiesFrom);
	}

	@Override
	public int getOrder() {
		return ORDER;
	}
}
