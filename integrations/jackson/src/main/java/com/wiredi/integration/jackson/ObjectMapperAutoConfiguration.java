package com.wiredi.integration.jackson;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wiredi.annotations.Provider;
import com.wiredi.annotations.stereotypes.AutoConfiguration;
import com.wiredi.integration.jackson.pagination.PaginationModule;
import com.wiredi.logging.Logging;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnEnabled;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnMissingBean;

import java.util.List;

@AutoConfiguration
@ConditionalOnEnabled("wiredi.autoconfig.jackson")
public class ObjectMapperAutoConfiguration {

    private static final Logging logger = Logging.getInstance(ObjectMapperAutoConfiguration.class);

    @Provider
    public PaginationModule pageableModule() {
        return new PaginationModule();
    }

    @Provider
    @ConditionalOnMissingBean(type = ObjectMapper.class)
    public ObjectMapper createObjectMapper(
            List<ObjectMapperConfigurer> configurers,
            List<Module> modules
    ) {
        logger.debug("Setting up default jackson ObjectMapper");
        ObjectMapper objectMapper = new ObjectMapper()
                .findAndRegisterModules()
                .registerModules(modules);
        configurers.forEach(configurer -> configurer.configure(objectMapper));

        return objectMapper;
    }
}
