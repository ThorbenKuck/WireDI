package com.wiredi.runtime.environment.builtin;

import com.google.auto.service.AutoService;
import com.wiredi.logging.Logging;
import com.wiredi.runtime.domain.Ordered;
import com.wiredi.runtime.Environment;
import com.wiredi.runtime.environment.EnvironmentConfiguration;
import com.wiredi.runtime.properties.TypedProperties;
import com.wiredi.runtime.resources.builtin.ClassPathResource;
import com.wiredi.runtime.resources.exceptions.ResourceException;
import org.jetbrains.annotations.NotNull;

import static com.wiredi.runtime.environment.DefaultEnvironmentKeys.*;

@AutoService(EnvironmentConfiguration.class)
public class ApplicationPropertiesEnvironmentConfiguration implements EnvironmentConfiguration {

    public static final int ORDER = Ordered.FIRST;
    private static final Logging LOGGER = Logging.getInstance(ApplicationPropertiesEnvironmentConfiguration.class);
    private static final String DEFAULT_PROPERTY_FILE_NAME = "application";

    @Override
    public void configure(@NotNull Environment environment) {
        TypedProperties environmentProperties = environment.properties();

        environmentProperties.getAll(DEFAULT_PROPERTIES, () -> environment.propertyLoader()
                        .supportedFileTypes()
                        .stream()
                        .map(fileType -> DEFAULT_PROPERTY_FILE_NAME + "." + fileType)
                        .toList()
                ).forEach(propertyPath -> {
                    ClassPathResource resource = new ClassPathResource(propertyPath);

                    try {
                        environment.appendPropertiesFrom(resource);
                    } catch (ResourceException ignore) {
                        LOGGER.debug(() -> "Failed to load properties from file " + resource.getFilename());
                    }
                });

        if (!environmentProperties.contains(ACTIVE_PROFILES)) {
            String defaultProfiles = environmentProperties
                    .get(DEFAULT_PROFILE)
                    .orElse("default");

            environment.setProperty(ACTIVE_PROFILES, defaultProfiles);
        }
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
