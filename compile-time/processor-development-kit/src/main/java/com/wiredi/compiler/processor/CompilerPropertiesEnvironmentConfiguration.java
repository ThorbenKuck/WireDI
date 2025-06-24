package com.wiredi.compiler.processor;

import com.google.auto.service.AutoService;
import com.wiredi.runtime.Environment;
import com.wiredi.runtime.environment.EnvironmentConfiguration;
import com.wiredi.runtime.resources.Resource;
import com.wiredi.runtime.resources.exceptions.ResourceException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

@AutoService(EnvironmentConfiguration.class)
public class CompilerPropertiesEnvironmentConfiguration implements EnvironmentConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(CompilerPropertiesEnvironmentConfiguration.class);
    private static final String DEFAULT_PROPERTY_FILE_NAME = "wire-di.processor";

    @Override
    public void configure(@NotNull Environment environment) {
        Collection<String> supportedFileTypes = environment.propertyLoader().supportedFileTypes();
        logger.debug("Loading properties from file " + DEFAULT_PROPERTY_FILE_NAME + " and supported file types:{}", supportedFileTypes);
        supportedFileTypes.stream()
                .map(fileType -> DEFAULT_PROPERTY_FILE_NAME + "." + fileType)
                .toList().forEach(propertyPath -> {
                    Resource resource = environment.resourceLoader().firstHitInAllResolvers(propertyPath);
                    if (resource != null && resource.exists()) {
                        try {
                            environment.appendPropertiesFrom(resource);
                        } catch (ResourceException ignore) {
                            logger.debug("Failed to load properties from file {}", resource.getFilename());
                        }
                    }
                });
    }
}
