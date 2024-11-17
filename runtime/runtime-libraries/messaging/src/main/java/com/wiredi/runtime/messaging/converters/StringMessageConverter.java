package com.wiredi.runtime.messaging.converters;

import com.wiredi.runtime.messaging.MessageHeaders;
import com.wiredi.runtime.messaging.Message;
import com.wiredi.runtime.messaging.MessageConverter;
import com.wiredi.runtime.messaging.MessageDetails;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A converter to convert between byte[] and String.
 */
public class StringMessageConverter implements MessageConverter<String, MessageDetails> {
    @Override
    public boolean canDeserialize(@NotNull Message<?> message, @NotNull Class<?> targetType) {
        return targetType == String.class;
    }

    @Override
    public String deserialize(@NotNull Message<MessageDetails> message, @NotNull Class<String> targetType) {
        return new String(message.body());
    }

    @Override
    public boolean canSerialize(@NotNull Object payload, @NotNull MessageHeaders headers, @Nullable MessageDetails messageDetails) {
        return payload instanceof String;
    }

    @Override
    public @Nullable Message<MessageDetails> serialize(@NotNull Object payload, @NotNull MessageHeaders headers, @Nullable MessageDetails messageDetails) {
        return Message.builder(((String) payload).getBytes())
                .addHeaders(headers)
                .withDetails(messageDetails)
                .build();
    }
}
