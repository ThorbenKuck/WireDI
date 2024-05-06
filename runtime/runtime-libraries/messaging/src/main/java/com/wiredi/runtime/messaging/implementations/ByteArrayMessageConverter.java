package com.wiredi.runtime.messaging.implementations;

import com.wiredi.runtime.messaging.Message;
import com.wiredi.runtime.messaging.MessageConverter;
import com.wiredi.runtime.messaging.MessageDetails;

/**
 * A simple pass through message converter to convert from byte[] to byte[].
 * <p>
 * This converter will not construct a new Message instance during (de)serialization.
 */
public class ByteArrayMessageConverter implements MessageConverter<byte[], MessageDetails> {
    @Override
    public Message<byte[], MessageDetails> deserialize(Message<byte[], MessageDetails> message, Class<byte[]> targetType) {
        return message;
    }

    @Override
    public Message<byte[], MessageDetails> serialize(Message<byte[], MessageDetails> message) {
        return message;
    }

    @Override
    public boolean canSerialize(Message<?, ?> message) {
        return message.getBody() instanceof byte[];
    }

    @Override
    public boolean canDeserialize(Message<byte[], ?> message, Class<?> targetType) {
        return targetType == byte[].class;
    }
}
