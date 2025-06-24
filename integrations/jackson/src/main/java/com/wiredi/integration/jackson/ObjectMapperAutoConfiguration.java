package com.wiredi.integration.jackson;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wiredi.annotations.Provider;
import com.wiredi.annotations.stereotypes.AutoConfiguration;
import com.wiredi.integration.jackson.pagination.PaginationModule;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnEnabled;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnMissingBean;

import java.util.List;

@AutoConfiguration
@ConditionalOnMissingBean(type = ObjectMapper.class)
@ConditionalOnEnabled("wiredi.autoconfig.jackson")
public class ObjectMapperAutoConfiguration {

    @Provider
    public PaginationModule pageableModule() {
        return new PaginationModule();
    }

    @Provider
    public ObjectMapper createObjectMapper(List<ObjectMapperConfigurer> configurers, List<Module> modules) {
        ObjectMapper objectMapper = new ObjectMapper()
                .findAndRegisterModules()
                .registerModules(modules);
        configurers.forEach(configurer -> configurer.configure(objectMapper));

        return objectMapper;
    }
}
