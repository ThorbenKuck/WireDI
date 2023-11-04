package com.wiredi.environment.builtin;

import com.google.auto.service.AutoService;
import com.wiredi.domain.Ordered;
import com.wiredi.environment.Environment;
import com.wiredi.resources.builtin.ClassPathResource;
import com.wiredi.environment.EnvironmentConfiguration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.wiredi.environment.DefaultEnvironmentKeys.*;
import static com.wiredi.lang.Preconditions.notNull;

@AutoService(EnvironmentConfiguration.class)
public class ApplicationPropertiesEnvironmentConfiguration implements EnvironmentConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(ApplicationPropertiesEnvironmentConfiguration.class);
	public static final int ORDER = Ordered.FIRST;

	@Override
	public void configure(@NotNull Environment environment) {
		String propertyPath = environment.getProperty(DEFAULT_PROPERTIES, "application.properties");
		notNull(propertyPath, () -> "Not Possible.. How? Why? Where?!");
		ClassPathResource resource = new ClassPathResource(propertyPath);

		if(resource.exists() && resource.isFile()) {
			environment.appendPropertiesFrom(resource);
		} else {
			logger.info("No default properties file " + propertyPath + " found in classpath.");
		}

		if (!environment.properties().contains(ACTIVE_PROFILES)) {
			if (Boolean.TRUE.equals(environment.properties().getBoolean(DEFAULT_PROFILE_ON_EMPTY))) {
				String defaultProfiles = environment.properties()
						.get(DEFAULT_PROFILES)
						.orElse("default");

				environment.setProperty(ACTIVE_PROFILES, defaultProfiles);
			}
		}
	}

	@Override
	public int getOrder() {
		return ORDER;
	}
}
