package com.wiredi.environment.builtin;

import com.wiredi.domain.Ordered;
import com.wiredi.environment.Environment;
import com.wiredi.properties.keys.Key;
import com.wiredi.resources.builtin.ClassPathResource;
import com.wiredi.environment.EnvironmentConfiguration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.wiredi.lang.Preconditions.notNull;

public class InsidePropertiesEnvironmentConfiguration implements EnvironmentConfiguration {

	private static final Key defaultProperties = Key.just("wire-di.default-properties");
	private static final Logger logger = LoggerFactory.getLogger(InsidePropertiesEnvironmentConfiguration.class);
	public static final int ORDER = Ordered.FIRST;

	@Override
	public void configure(@NotNull Environment environment) {
		String propertyPath = environment.getProperty(defaultProperties, "application.properties");
		notNull(propertyPath, () -> "Not Possible.. How? Why? Where?!");
		ClassPathResource resource = new ClassPathResource(propertyPath);

		if(resource.exists() && resource.isFile()) {
			environment.appendPropertiesFrom(resource);
		} else {
			logger.info("No default properties file " + propertyPath + " found in classpath.");
		}
	}
}
