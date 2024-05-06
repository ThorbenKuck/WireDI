package com.wiredi.runtime.environment.builtin;

import com.google.auto.service.AutoService;
import com.wiredi.runtime.domain.Ordered;
import com.wiredi.runtime.Environment;
import com.wiredi.runtime.environment.EnvironmentConfiguration;
import com.wiredi.runtime.resources.builtin.ClassPathResource;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

import static com.wiredi.runtime.environment.DefaultEnvironmentKeys.ACTIVE_PROFILES;

@AutoService(EnvironmentConfiguration.class)
public class ProfilePropertiesEnvironmentConfiguration implements EnvironmentConfiguration {

    public static final int ORDER = Ordered.after(AdditionalPropertiesEnvironmentConfiguration.ORDER);

    @Override
    public void configure(@NotNull Environment environment) {
        List<String> activeProfiles = environment.properties()
                .getAll(ACTIVE_PROFILES);
        Collection<String> supportedFileTypes = environment.propertyLoader().supportedFileTypes();

        activeProfiles.parallelStream()
                .flatMap(profile -> supportedFileTypes
                        .stream()
                        .map(type -> "application-" + profile + "." + type))
                .map(ClassPathResource::new)
                .filter(ClassPathResource::exists)
                .filter(ClassPathResource::isFile)
                .forEach(environment::appendPropertiesFrom);
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
