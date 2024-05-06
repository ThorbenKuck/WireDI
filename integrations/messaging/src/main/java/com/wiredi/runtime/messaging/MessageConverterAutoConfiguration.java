package com.wiredi.runtime.messaging;

import com.wiredi.annotations.Provider;
import com.wiredi.annotations.stereotypes.AutoConfiguration;
import com.wiredi.runtime.messaging.implementations.ByteArrayMessageConverter;
import com.wiredi.runtime.messaging.implementations.StringMessageConverter;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnMissingBean;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnProperty;

import java.util.List;

@AutoConfiguration
@ConditionalOnProperty(
        key = "wiredi.conversions.autoconfigure",
        havingValue = "true",
        matchIfMissing = true
)
public class MessageConverterAutoConfiguration {

    @Provider
    @ConditionalOnMissingBean(type = MessagingEngine.class)
    public MessagingEngine messageConverters(
            List<MessageConverter<?, ?>> messageConverter
    ) {
        return new CompositeMessageEngine(messageConverter);
    }

    @Provider
    @ConditionalOnMissingBean(type = HeadersAccessor.class)
    public HeadersAccessor headersAccessor() {
        return new HeadersAccessor();
    }

    @Provider
    public StringMessageConverter stringMessageConverter() {
        return new StringMessageConverter();
    }

    @Provider
    public ByteArrayMessageConverter byteArrayMessageConverter() {
        return new ByteArrayMessageConverter();
    }
}
