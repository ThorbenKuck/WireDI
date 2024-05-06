package com.wiredi.integration.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface ObjectMapperConfigurer {

    void configure(ObjectMapper objectMapper);

}
