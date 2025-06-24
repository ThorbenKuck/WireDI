package com.wiredi.integration.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wiredi.annotations.Order;
import com.wiredi.annotations.Provider;
import com.wiredi.annotations.stereotypes.AutoConfiguration;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnBean;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnEnabled;

@AutoConfiguration
@ConditionalOnBean(type = ObjectMapper.class)
@ConditionalOnEnabled("wiredi.autoconfig.jackson")
public class MessageConversionAutoConfiguration {

    @Provider
    @Order(Order.LAST - 10)
    public JacksonMessageConverter jacksonMessageConverter(ObjectMapper objectMapper) {
        return new JacksonMessageConverter(objectMapper);
    }
}
