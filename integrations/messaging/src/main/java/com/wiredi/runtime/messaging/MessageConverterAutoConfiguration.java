package com.wiredi.runtime.messaging;

import com.wiredi.annotations.Provider;
import com.wiredi.annotations.stereotypes.AutoConfiguration;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnMissingBean;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnProperty;
import com.wiredi.runtime.messaging.compression.MessageCompression;
import com.wiredi.runtime.messaging.compression.MessageCompressionAlgorithm;
import com.wiredi.runtime.messaging.converters.ByteArrayMessageConverter;
import com.wiredi.runtime.messaging.converters.StringMessageConverter;

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
            MessagingContext messageEngineContext,
            RequestContext requestcontext
    ) {
        return new CompositeMessagingEngine(messageEngineContext, requestcontext);
    }

    @Provider
    @ConditionalOnMissingBean(type = MessageHeadersAccessor.class)
    public MessageHeadersAccessor messageHeadersAccessor() {
        return new MessageHeadersAccessor();
    }

    @Provider
    @ConditionalOnMissingBean(type = MessagingErrorHandler.class)
    public MessagingErrorHandler defaultErrorHandler() {
        return MessagingErrorHandler.DEFAULT;
    }

    @Provider
    @ConditionalOnMissingBean(type = MessagingContext.class)
    public MessagingContext messageEngineContext(
            List<MessageConverter<?, ?>> converters,
            List<MessageInterceptor> messageInterceptors
    ) {
        return new MessagingContext(converters, messageInterceptors);
    }

    @Provider
    @ConditionalOnMissingBean(type = MessageCompression.class)
    public MessageCompression messageCompression(
            List<MessageCompressionAlgorithm> messageCompressionAlgorithms,
            List<MessageCompressionConfiguration> configurations
    ) {
        MessageCompression compression = new MessageCompression(messageCompressionAlgorithms);
        configurations.forEach(configuration -> configuration.configure(compression));
        return compression;
    }

    @Provider
    @ConditionalOnMissingBean(type = RequestContext.class)
    public RequestContext requestContext(
            List<RequestAware> requestAwareList,
            List<MessageFilter> messageFilters,
            MessagingErrorHandler messagingErrorHandler,
            MessageHeadersAccessor headersAccessor
    ) {
        return new RequestContext(requestAwareList, messageFilters, messagingErrorHandler, headersAccessor, );
    }

    @Provider
    @ConditionalOnMissingBean(type = MessageHeadersAccessor.class)
    public MessageHeadersAccessor headersAccessor() {
        return new MessageHeadersAccessor();
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
