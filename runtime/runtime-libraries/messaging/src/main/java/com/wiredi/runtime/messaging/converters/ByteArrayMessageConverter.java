package com.wiredi.runtime.messaging.converters;

import com.wiredi.runtime.messaging.MessageHeaders;
import com.wiredi.runtime.messaging.Message;
import com.wiredi.runtime.messaging.MessageConverter;
import com.wiredi.runtime.messaging.MessageDetails;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A simple pass through message converter to convert from byte[] to byte[].
 * <p>
 * This converter will not construct a new Message instance during (de)serialization.
 */
public class ByteArrayMessageConverter implements MessageConverter<byte[], MessageDetails> {
    @Override
    public boolean canDeserialize(@NotNull Message<?> message, @NotNull Class<?> targetType) {
        return targetType == byte[].class;
    }

    @Override
    public byte @Nullable [] deserialize(@NotNull Message<MessageDetails> message, @NotNull Class<byte[]> targetType) {
        return message.body();
    }

    @Override
    public boolean canSerialize(@NotNull Object payload, @NotNull MessageHeaders headers, @NotNull MessageDetails messageDetails) {
        return payload instanceof byte[];
    }

    @Override
    public @Nullable Message<MessageDetails> serialize(@NotNull Object payload, @NotNull MessageHeaders headers, @NotNull MessageDetails messageDetails) {
        return Message.builder((byte[]) payload)
                .addHeaders(headers)
                .withDetails(messageDetails)
                .build();
    }
}
