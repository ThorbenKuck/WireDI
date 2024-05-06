package com.wiredi.runtime.messaging.implementations;

import com.wiredi.runtime.messaging.Message;
import com.wiredi.runtime.messaging.MessageConverter;
import com.wiredi.runtime.messaging.MessageDetails;

/**
 * A converter to convert between byte[] and String.
 */
public class StringMessageConverter implements MessageConverter<String, MessageDetails> {
    @Override
    public Message<String, MessageDetails> deserialize(Message<byte[], MessageDetails> message, Class<String> targetType) {
        return message.map(String::new);
    }

    @Override
    public Message<byte[], MessageDetails> serialize(Message<String, MessageDetails> message) {
        return message.map(String::getBytes);
    }

    @Override
    public boolean canSerialize(Message<?, ?> message) {
        return message.getBody() instanceof String;
    }

    @Override
    public boolean canDeserialize(Message<byte[], ?> message, Class<?> targetType) {
        return targetType == String.class;
    }
}
