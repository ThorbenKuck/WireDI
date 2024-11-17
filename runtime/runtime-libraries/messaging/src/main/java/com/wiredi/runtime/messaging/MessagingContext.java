package com.wiredi.runtime.messaging;

import com.wiredi.runtime.lang.MapId;
import com.wiredi.runtime.lang.ThrowingFunction;
import com.wiredi.runtime.messaging.errors.MessagingException;
import com.wiredi.runtime.messaging.converters.ByteArrayMessageConverter;
import com.wiredi.runtime.messaging.converters.StringMessageConverter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessagingContext {

    private final Map<MapId, MessageConverter<?, ?>> lastHitConverts = new ConcurrentHashMap<>();
    private final List<MessageConverter<?, ?>> converters;
    private final List<MessageInterceptor> messageInterceptors;
    private static final MessagingContext GLOBAL_INSTANCE = new MessagingContext(
            new ArrayList<>(
                    List.of(new ByteArrayMessageConverter(), new StringMessageConverter())
            ),
            new ArrayList<>()
    );

    public MessagingContext(
            List<MessageConverter<?, ?>> converters,
            List<MessageInterceptor> messageInterceptors
    ) {
        this.converters = converters;
        this.messageInterceptors = messageInterceptors;
    }

    public static MessagingContext empty() {
        return new MessagingContext(
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    public static MessagingContext defaultContext() {
        return GLOBAL_INSTANCE;
    }

    @NotNull
    public <T, D extends MessageDetails> T convertCacheAware(
            Class<?> targetType,
            D details,
            ThrowingFunction<MessageConverter<?, ?>, T, ?> function
    ) {
        MapId id = MapId.of(targetType).add(details.getClass());
        MessageConverter<?, ?> lastHit = lastHitConverts.get(id);
        List<Throwable> errors = new ArrayList<>();

        if (lastHit != null) {
            try {
                T result = function.apply(lastHit);
                if (result != null) {
                    return result;
                }
            } catch (Throwable t) {
                errors.add(t);
            }
            lastHitConverts.remove(id);
        }

        for (MessageConverter<?, ?> converter : converters) {
            try {
                T result = function.apply(converter);
                if (result != null) {
                    lastHitConverts.put(id, converter);
                    return result;
                }
            } catch (Throwable t) {
                errors.add(t);
            }
        }

        MessagingException exception = new MessagingException("Unable to find converter to convert type " + targetType + " with details " + details);
        errors.forEach(exception::addSuppressed);
        throw exception;
    }

    public List<MessageConverter<?,?>> converters() {
        return converters;
    }

    public List<MessageInterceptor> messageInterceptors() {
        return messageInterceptors;
    }
}
