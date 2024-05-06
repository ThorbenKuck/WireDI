package com.wiredi.integration.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.wiredi.integration.jackson.exceptions.DeserializationFailedException;
import com.wiredi.integration.jackson.exceptions.SerializationFailedException;
import com.wiredi.runtime.messaging.Message;
import com.wiredi.runtime.messaging.MessageConverter;
import com.wiredi.runtime.messaging.MessageDetails;

import java.io.IOException;

public class JacksonMessageConverter implements MessageConverter<Object, MessageDetails> {

    private final ObjectMapper objectMapper;

    public JacksonMessageConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JacksonMessageConverter() {
        this(new ObjectMapper().findAndRegisterModules());
    }

    @Override
    public Message<Object, MessageDetails> deserialize(Message<byte[], MessageDetails> message, Class<Object> targetType) {
        try {
            return message.map(it -> objectMapper.readValue(it, targetType));
        } catch (IOException e) {
            throw new DeserializationFailedException(message, targetType, e);
        }
    }

    @Override
    public Message<byte[], MessageDetails> serialize(Message<Object, MessageDetails> message) {
        try {
            return message.map(objectMapper::writeValueAsBytes);
        } catch (JsonProcessingException e) {
            throw new SerializationFailedException(message, e);
        }
    }

    @Override
    public boolean canSerialize(Message<?, ?> message) {
        return objectMapper.canSerialize(message.getBody().getClass());
    }

    @Override
    public boolean canDeserialize(Message<byte[], ?> obj, Class<?> targetType) {
        return objectMapper.canDeserialize(SimpleType.constructUnsafe(targetType));
    }
}
