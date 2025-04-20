package com.wiredi.runtime.messaging.messages;

import com.wiredi.runtime.messaging.Message;
import com.wiredi.runtime.messaging.MessageDetails;
import com.wiredi.runtime.messaging.MessageHeader;
import com.wiredi.runtime.messaging.MessageHeaders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;

/**
 * This is an implementation of the {@link Message} which takes the body from an input stream.
 * <p>
 * The underlying {@link InputStream} will not be buffered.
 * Repeated calls to {@link #body()} or repeated reads from the {@link #inputStream()} will likely result in an
 * Exceptions to be raised.
 * <p>
 * The method {@link #buffer()} can be used to construct a new {@link Message} that contains the contents from the
 * {@link InputStream}.
 * It is not recommended though.
 *
 * @param <D> the generic type of {@link MessageDetails} of this message
 * @see MessageHeaders
 * @see MessageDetails
 * @see Message
 * @see SimpleMessage
 */
public class InputStreamMessage<D extends MessageDetails> extends AbstractMessage<D> {

    @NotNull
    private final InputStream inputStream;

    public InputStreamMessage(
            @NotNull InputStream inputStream,
            @NotNull MessageHeaders headers,
            @NotNull D messageDetails,
            boolean chunked
    ) {
        super(headers, messageDetails, chunked);
        this.inputStream = inputStream;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] body() {
        try {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long bodySize() {
        try {
            return inputStream().available();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeBodyTo(OutputStream outputStream) throws IOException {
        inputStream.transferTo(outputStream);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull InputStream inputStream() {
        return inputStream;
    }

    public static class Builder<S extends MessageDetails> {

        private final MessageHeaders.Builder headers = MessageHeaders.builder();
        private final InputStream inputStream;
        @NotNull
        private MessageDetails messageDetails = MessageDetails.NONE;

        public Builder(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @NotNull
        public MessageHeaders.Builder headers() {
            return headers;
        }

        @NotNull
        public Builder<S> addHeader(@NotNull String key, @NotNull String value) {
            headers.add(key, value);
            return this;
        }

        @NotNull
        public Builder<S> addHeader(@NotNull String key, byte[] value) {
            headers.add(key, value);
            return this;
        }

        @NotNull
        public Builder<S> addHeader(@NotNull MessageHeader entry) {
            headers.add(entry);
            return this;
        }

        @NotNull
        public Builder<S> addHeaders(@Nullable MessageHeaders headers) {
            if (headers != null) {
                this.headers.addAll(headers);
            }
            return this;
        }

        @NotNull
        public Builder<S> addHeaders(@Nullable Iterable<MessageHeader> headers) {
            if (headers != null) {
                this.headers.addAll(headers);
            }
            return this;
        }

        @NotNull
        public <D extends MessageDetails> Builder<D> withDetails(@NotNull D details) {
            this.messageDetails = details;
            return (Builder<D>) this;
        }

        @NotNull
        public InputStreamMessage<S> build() {
            return new InputStreamMessage<>(
                    inputStream,
                    headers.build(),
                    (S) messageDetails,
                    true
            );
        }
    }
}
