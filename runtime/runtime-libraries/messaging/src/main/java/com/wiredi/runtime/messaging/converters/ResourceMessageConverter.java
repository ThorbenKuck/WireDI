package com.wiredi.runtime.messaging.converters;

import com.wiredi.runtime.messaging.Message;
import com.wiredi.runtime.messaging.MessageConverter;
import com.wiredi.runtime.messaging.MessageDetails;
import com.wiredi.runtime.messaging.MessageHeaders;
import com.wiredi.runtime.resources.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ResourceMessageConverter implements MessageConverter<Resource, MessageDetails> {

    @Override
    public @Nullable Resource deserialize(@NotNull Message message, @NotNull Class targetType) {
        return null;
    }

    @Override
    public @Nullable Message<MessageDetails> serialize(@NotNull Object payload, @NotNull MessageHeaders headers, @NotNull MessageDetails messageDetails) {
        if (payload instanceof Resource resource) {
            if (resource.exists()) {
                return Message.builder(resource.getInputStream())
                        .withDetails(messageDetails)
                        .addHeaders(headers)
                        .build();
            }
        }

        return null;
    }
}
