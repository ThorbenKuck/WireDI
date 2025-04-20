package com.wiredi.integration.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.wiredi.integration.jackson.exceptions.DeserializationFailedException;
import com.wiredi.integration.jackson.exceptions.SerializationFailedException;
import com.wiredi.runtime.messaging.MessageHeaders;
import com.wiredi.runtime.messaging.Message;
import com.wiredi.runtime.messaging.MessageConverter;
import com.wiredi.runtime.messaging.MessageDetails;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JacksonMessageConverter implements MessageConverter<Object, MessageDetails> {

    private final ObjectMapper objectMapper;
    private final Map<Class<?>, ObjectWriter> writerCache = new HashMap<>();
    private final Map<Class<?>, ObjectReader> readerCache = new HashMap<>();

    public JacksonMessageConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean canDeserialize(@NotNull Message<?> message, @NotNull Class<?> targetType) {
        return objectMapper.canDeserialize(SimpleType.constructUnsafe(targetType));
    }

    @Override
    public Object deserialize(@NotNull Message<MessageDetails> message, @NotNull Class<Object> targetType) {
        ObjectReader reader = getReader(targetType);
        try {
            return reader.readValue(message.body());
        } catch (IOException e) {
            throw new DeserializationFailedException(message, targetType, e);
        }
    }

    @Override
    public boolean canSerialize(@NotNull Object payload, @NotNull MessageHeaders headers, @Nullable MessageDetails messageDetails) {
        return objectMapper.canSerialize(payload.getClass());
    }

    @Override
    public @Nullable Message<MessageDetails> serialize(@NotNull Object payload, @NotNull MessageHeaders headers, @NotNull MessageDetails messageDetails) {
        try {
            byte[] serialized = getWriter(payload.getClass()).writeValueAsBytes(payload);
            return Message.builder(serialized)
                    .withDetails(messageDetails)
                    .addHeaders(headers)
                    .build();
        } catch (JsonProcessingException e) {
            throw new SerializationFailedException(payload, e);
        }
    }

    private ObjectWriter getWriter(Class<?> type) {
        return writerCache.computeIfAbsent(type, objectMapper::writerFor);
    }

    private ObjectReader getReader(Class<?> type) {
        return readerCache.computeIfAbsent(type, objectMapper::readerFor);
    }
}
