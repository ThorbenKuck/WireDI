package com.wiredi.runtime.messaging;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.lang.ThrowingFunction;
import com.wiredi.runtime.messaging.errors.MissingMessageConverterException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This {@link MessagingEngine} instance holds a list of {@link MessageConverter} instances and checks them in order.
 * <p>
 * Every call to {@link #deserialize(Message, Class)} and {@link #serialize(Object, MessageHeaders, MessageDetails)} will check all {@link MessagingContext#converters()}
 * for the first converter that is able to convert the {@link Message}.
 * <p>
 * {@link MessageConverter} are cached whenever a converter successfully serializes or deserializes a message.
 * They are cached for the combination of the target class plus the {@link MessageDetails} type.
 * Next accesses to the same combination of target class plus {@link MessageDetails} type will first try to use the
 * last hit {@link MessageConverter} and only if it is not applicable search again for another {@link MessageConverter}.
 */
public class CompositeMessagingEngine implements MessagingEngine {

    private static final Logging logger = Logging.getInstance(CompositeMessagingEngine.class);
    private final MessagingContext messageEngineContext;
    private final RequestContext requestContext;

    public CompositeMessagingEngine(
            MessagingContext messageEngineContext,
            RequestContext requestContext
    ) {
        this.messageEngineContext = messageEngineContext;
        this.requestContext = requestContext;
    }

    public CompositeMessagingEngine(
            RequestContext requestContext
    ) {
        this(MessagingContext.defaultContext(), requestContext);
    }

    public CompositeMessagingEngine(
            MessagingContext messageEngineContext
    ) {
        this(messageEngineContext, RequestContext.defaultInstance());
    }

    public CompositeMessagingEngine() {
        this(MessagingContext.defaultContext(), RequestContext.defaultInstance());
    }

    @Override
    public <E extends Throwable, D extends MessageDetails> MessagingResult processMessage(Message<D> message, ThrowingFunction<Message<D>, Object, E> handler) {
        try {
            return requestContext.handleRequest(message, () -> handler.apply(message));
        } catch (Throwable e) {
            logger.error(() -> "Unexpected error thrown during processing of message " + message, e);
            return new MessagingResult.Failed(e);
        }
    }

    @Override
    public <T, S extends MessageDetails> @NotNull T deserialize(@NotNull Message<S> rawMessage, @NotNull Class<T> targetType) throws MissingMessageConverterException {
        return messageEngineContext.convertCacheAware(targetType, rawMessage.details(), converter -> {
            if (converter.canDeserialize(rawMessage, targetType)) {
                return ((MessageConverter<T, S>) converter).deserialize(rawMessage, targetType);
            } else {
                return null;
            }
        });
    }

    @Override
    @NotNull
    public <S extends MessageDetails> Message<S> serialize(
            @Nullable Object payload,
            @NotNull MessageHeaders headers,
            @NotNull S details
    ) throws MissingMessageConverterException {
        Message<S> message;

        if (payload == null) {
            message = Message.newEmptyMessage()
                    .withDetails(details)
                    .addHeaders(headers)
                    .build();
        } else {
            message = messageEngineContext.convertCacheAware(payload.getClass(), details, converter -> {
                if (converter.canSerialize(payload, headers, details)) {
                    return ((MessageConverter<?, S>) converter).serialize(payload, headers, details);
                } else {
                    return null;
                }
            });
        }

        return postProcess(message);
    }

    @Override
    public @NotNull MessagingContext messageEngineContext() {
        return messageEngineContext;
    }

    @Override
    public @NotNull RequestContext requestContext() {
        return requestContext;
    }

    public <D extends MessageDetails> Message<D> postProcess(Message<D> message) {
        Message<D> toProcess = message;
        for (MessageInterceptor messageInterceptor : messageEngineContext.messageInterceptors()) {
            toProcess = messageInterceptor.postConstruction(toProcess);
        }
        return toProcess;
    }
}
