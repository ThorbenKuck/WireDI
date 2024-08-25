package com.wiredi.runtime.messaging;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.messaging.errors.MissingMessageConverterException;
import com.wiredi.runtime.lang.MapId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This {@link MessagingEngine} instance holds a list of {@link MessageConverter} instances and checks them in order.
 * <p>
 * Every call to {@link #deserialize(Message, Class)} and {@link #serialize(Message)} will check all {@link #converters}
 * for the first converter that is able to convert the {@link Message}.
 * <p>
 * {@link MessageConverter} are cached whenever a converter successfully serializes or deserializes a message.
 * They are cached for the combination of the target class plus the {@link MessageDetails} type.
 * Next accesses to the same combination of target class plus {@link MessageDetails} type will first try to use the
 * last hit {@link MessageConverter} and only if it is not applicable search again for another {@link MessageConverter}.
 */
public class CompositeMessagingEngine implements MessagingEngine {

    private final Map<MapId, MessageConverter<?, ?>> lastHitConverts = new ConcurrentHashMap<>();
    private final List<MessageConverter<?, ?>> converters;
    private static final Logging logger = Logging.getInstance(CompositeMessagingEngine.class);

    public CompositeMessagingEngine(List<MessageConverter<?, ?>> converters) {
        this.converters = new ArrayList<>(converters);
    }

    private <T, S extends MessageDetails> MessageConverter<T, S> getLastHitConverter(MapId mapId) {
        return (MessageConverter<T, S>) lastHitConverts.get(mapId);
    }

    @Override
    @NotNull
    public <T, S extends MessageDetails> Message<T, S> deserialize(@NotNull Message<byte[], S> rawMessage, @NotNull Class<T> targetType) throws MissingMessageConverterException {
        MapId mapId = MapId.of(targetType).add(detailsType(rawMessage.getDetails()));
        Message<T, S> lastHit = deserializeFromLastHist(mapId, rawMessage, targetType);
        if (lastHit != null) {
            return lastHit;
        }

        for (MessageConverter converter : converters) {
            if (converter.canDeserialize(rawMessage, targetType)) {
                Message deserialized = converter.deserialize(rawMessage, targetType);
                if (deserialized != null) {
                    cache(mapId, converter);
                    return deserialized;
                }
            }
        }

        throw new MissingMessageConverterException(rawMessage);
    }

    @Nullable
    private <T, S extends MessageDetails> Message<T, S> deserializeFromLastHist(MapId mapId, Message<byte[], S> message, Class<T> targetType) {
        MessageConverter<T, S> lastHit = getLastHitConverter(mapId);
        if (lastHit != null && lastHit.canDeserialize(message, targetType)) {
            Message<T, S> serialized = lastHit.deserialize(message, targetType);
            if (serialized != null) {
                logger.debug(() -> "Successful cache hit during deserialization for " + mapId);
                return serialized;
            } else {
                logger.debug(() -> "Invaliding cache hit for " + mapId);
                lastHitConverts.remove(mapId);
            }
        }

        return null;
    }

    @Override
    @NotNull
    public <T, S extends MessageDetails> Message<byte[], S> serialize(@NotNull Message<T, S> message) throws MissingMessageConverterException {
        if (message.getBody() instanceof byte[]) {
            return (Message<byte[], S>) message;
        }
        MapId mapId = MapId.of(message.getBody().getClass()).add(detailsType(message.getDetails()));
        Message<byte[], S> lastHit = serializeFromLastHist(mapId, message);
        if (lastHit != null) {
            return lastHit;
        }

        for (MessageConverter converter : converters) {
            if (converter.canSerialize(message)) {
                Message serialized = converter.serialize(message);
                if (serialized != null) {
                    cache(mapId, converter);
                    return serialized;
                }
            }
        }

        throw new MissingMessageConverterException(message);
    }

    @Nullable
    private <T, S extends MessageDetails> Message<byte[], S> serializeFromLastHist(MapId mapId, Message<T, S> message) {
        MessageConverter<T, S> lastHit = getLastHitConverter(mapId);
        if (lastHit != null && lastHit.canSerialize(message)) {
            Message<byte[], S> serialized = lastHit.serialize(message);
            if (serialized != null) {
                logger.debug(() -> "Successful cache hit during serialization for " + mapId);
                return serialized;
            } else {
                logger.debug(() -> "Invaliding cache hit for " + mapId);
                lastHitConverts.remove(mapId);
            }
        }

        return null;
    }

    private void cache(MapId mapId, MessageConverter<?, ?> messageConverter) {
        this.lastHitConverts.put(mapId, messageConverter);
        logger.debug(() -> "Setting cache value for " + mapId + ". New cache size: " + lastHitConverts.size());
    }

    private Class<? extends MessageDetails> detailsType(@Nullable MessageDetails details) {
        if (details == null) {
            return MessageDetails.class;
        } else {
            return details.getClass();
        }
    }
}
