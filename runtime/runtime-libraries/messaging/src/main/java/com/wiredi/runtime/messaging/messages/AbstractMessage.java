package com.wiredi.runtime.messaging.messages;

import com.wiredi.runtime.messaging.Message;
import com.wiredi.runtime.messaging.MessageDetails;
import com.wiredi.runtime.messaging.MessageHeaders;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

public abstract class AbstractMessage<D extends MessageDetails> implements Message<D> {

    private final @NotNull MessageHeaders headers;
    private final D messageDetails;
    private boolean chunked = false;

    public AbstractMessage(
            @NotNull MessageHeaders headers,
            @NotNull D messageDetails,
            boolean chunked
    ) {
        this.headers = headers;
        this.messageDetails = messageDetails;
        this.chunked = chunked;
    }

    public AbstractMessage(
            @NotNull MessageHeaders headers,
            @NotNull D messageDetails
    ) {
        this.headers = headers;
        this.messageDetails = messageDetails;
    }

    @Override
    public @NotNull MessageHeaders headers() {
        return headers;
    }

    @Override
    public @NotNull D details() {
        return messageDetails;
    }

    @Override
    public Message<D> copyWithPayload(InputStream inputStream) {
        return new InputStreamMessage<>(inputStream, headers, messageDetails, chunked);
    }

    @Override
    public Message<D> copyWithPayload(byte[] bytes) {
        return new SimpleMessage<>(bytes, headers, messageDetails);
    }

    @Override
    public boolean isChunked() {
        return chunked;
    }

    @Override
    public Message<D> setChunked(boolean chunked) {
        this.chunked = chunked;
        return this;
    }
}
