package com.wiredi.runtime.environment.builtin;

import com.google.auto.service.AutoService;
import com.wiredi.logging.Logging;
import com.wiredi.runtime.domain.Ordered;
import com.wiredi.runtime.Environment;
import com.wiredi.runtime.environment.EnvironmentConfiguration;
import com.wiredi.runtime.resources.builtin.ClassPathResource;
import com.wiredi.runtime.resources.exceptions.ResourceException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.wiredi.runtime.environment.DefaultEnvironmentKeys.*;

@AutoService(EnvironmentConfiguration.class)
public class ApplicationPropertiesEnvironmentConfiguration implements EnvironmentConfiguration {

    public static final int ORDER = Ordered.FIRST;
    private static final Logging LOGGER = Logging.getInstance(ApplicationPropertiesEnvironmentConfiguration.class);
    private static final String DEFAULT_PROPERTY_FILE_NAME = "application";

    @Override
    public void configure(@NotNull Environment environment) {
        environment.propertyLoader().supportedFileTypes();

        environment.properties().getAll(DEFAULT_PROPERTIES, () -> environment.propertyLoader()
                        .supportedFileTypes()
                        .stream()
                        .map(fileType -> DEFAULT_PROPERTY_FILE_NAME + "." + fileType)
                        .toList()
                ).forEach(propertyPath -> {
                    ClassPathResource resource = new ClassPathResource(propertyPath);

                    try {
                        environment.appendPropertiesFrom(resource);
                    } catch (ResourceException ignore) {
                        LOGGER.debug("");
                    }
                });

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
