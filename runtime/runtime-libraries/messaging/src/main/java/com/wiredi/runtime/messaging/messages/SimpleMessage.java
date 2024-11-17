package com.wiredi.runtime.messaging.messages;

import com.wiredi.runtime.lang.ThrowingConsumer;
import com.wiredi.runtime.lang.ThrowingFunction;
import com.wiredi.runtime.messaging.Message;
import com.wiredi.runtime.messaging.MessageDetails;
import com.wiredi.runtime.messaging.MessageHeader;
import com.wiredi.runtime.messaging.MessageHeaders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This implementation of the {@link Message} simply holds a byte array as its content.
 * <p>
 * This most common implementation is also considered buffered, meaning that repeated call to {@link #body()} yield the
 * same results and calls to {@link #inputStream()} construct a new {@link InputStream}.
 *
 * @param <S> the subtype of the {@link MessageDetails}
 * @see MessageHeaders
 * @see MessageDetails
 * @see Message
 * @see SimpleMessage
 */
public class SimpleMessage<S extends MessageDetails> implements Message<S> {

    private final byte[] body;
    @NotNull
    private final MessageHeaders headers;
    @NotNull
    private final S messageDetails;

    public SimpleMessage(
            byte[] body,
            @NotNull MessageHeaders headers,
            @NotNull S messageDetails
    ) {
        this.headers = headers;
        this.body = body;
        this.messageDetails = messageDetails;
    }

    /**
     * Creates a new Builder for a message based on the {@code body}
     *
     * @param body the body that should be contained in the message.
     * @return a new {@link Builder} to construct the Message
     */
    public static SimpleMessage.Builder<MessageDetails> of(byte[] body) {
        return new Builder<>(body);
    }

    /**
     * Creates a new Builder for a message without any previous details.
     *
     * @return a new {@link Builder} to construct the Message
     */
    public static SimpleMessage.Builder<MessageDetails> build() {
        return new Builder<>(null);
    }

    /**
     * Quickly construct a new message with just a body.
     * <p>
     * The resulting message will have no {@link MessageHeaders} and no {@link MessageDetails}.
     * Using the mapping functions, these can later be added.
     * <p>
     * However, if {@link MessageHeaders} and/or {@link MessageDetails} are known during message construction, consider using
     * the {@link #builder(byte[])} method to reduce the objects that are being created.
     *
     * @param body the body to wrap in the {@link SimpleMessage}
     * @return a new {@link SimpleMessage} containing only the {@code body}
     * @see #builder(byte[])
     */
    public static SimpleMessage<MessageDetails> just(byte[] body) {
        return new Builder<>(body).build();
    }

    /**
     * This class is buffered, no need to construct a new instance.
     *
     * @return this reference
     */
    @Override
    public @NotNull Message<S> buffer() {
        return this;
    }

    /**
     * Returns the message headers.
     *
     * @return the headers set for this message.
     */
    @Override
    public @NotNull MessageHeaders headers() {
        return headers;
    }

    /**
     * Returns the body of this message.
     *
     * @return the body of this message.
     */
    @Override
    public byte[] body() {
        return body;
    }

    @Override
    public long bodySize() {
        return body.length;
    }

    /**
     * Returns the details of this message.
     *
     * @return the details of this message.
     */
    @Override
    public @NotNull S details() {
        return messageDetails;
    }

    @Override
    public void writeBodyTo(OutputStream outputStream) throws IOException {
        outputStream.write(body);
    }

    @Override
    public @NotNull InputStream inputStream() {
        return new ByteArrayInputStream(body);
    }

    /**
     * Maps the details of this message and constructs a new instance of the {@link SimpleMessage}.
     * <p>
     * The references of the {@code body} and {@link MessageHeaders} in the new message will be the same as in this one.
     *
     * @param mapper      the mapping function to map the body.
     * @param <D>         The generic type of the mapped body.
     * @param <THROWABLE> (Optional) a generic of any throwable that the mapping function can throw.
     * @return a new message containing the mapped body.
     * @throws THROWABLE if the {@link ThrowingFunction} throws the provided throwable
     * @see ThrowingFunction
     */
    public <D extends MessageDetails, THROWABLE extends Throwable> @NotNull SimpleMessage<D> mapDetails(@NotNull ThrowingFunction<@NotNull S, @NotNull D, THROWABLE> mapper) throws THROWABLE {
        return new SimpleMessage<>(
                body,
                headers,
                mapper.apply(messageDetails)
        );
    }

    /**
     * Maps the headers of this message and constructs a new instance of the {@link SimpleMessage}.
     * <p>
     * The references of the {@code body} and {@link MessageDetails} in the new message will be the same as in this one.
     *
     * @param constructor the consumer to modify the headers
     * @param <THROWABLE> (Optional) a generic of any throwable that the mapping function can throw.
     * @return a new message containing the mapped body.
     * @throws THROWABLE if the {@link ThrowingFunction} throws the provided throwable
     * @see ThrowingConsumer
     */
    public <THROWABLE extends Throwable> @NotNull SimpleMessage<S> mapHeaders(@NotNull ThrowingConsumer<@NotNull MessageHeaders, THROWABLE> constructor) throws THROWABLE {
        MessageHeaders copy = headers.copy().build();
        constructor.accept(copy);
        return new SimpleMessage<>(
                body,
                copy,
                messageDetails
        );
    }

    @Override
    public String toString() {
        List<String> fieldValues = new ArrayList<>();
        fieldValues.add("body=" + Arrays.toString(body));
        if (!headers.isEmpty()) {
            fieldValues.add("headers=" + headers);
        }
        if (messageDetails.isNotNone()) {
            fieldValues.add("messageDetails=" + messageDetails);
        }
        return "Message{" +
                String.join(", ", fieldValues) +
                '}';
    }

    public static class Builder<S extends MessageDetails> {

        private final MessageHeaders.Builder headers = MessageHeaders.builder();
        private final byte[] body;
        @NotNull
        private MessageDetails messageDetails = MessageDetails.NONE;

        public Builder(byte[] body) {
            this.body = body;
        }

        public byte[] body() {
            if (body == null) {
                throw new IllegalStateException("Tried to build a message without a body");
            }
            return body;
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
        public SimpleMessage<S> build() {
            return new SimpleMessage<>(
                    body(),
                    headers.build(),
                    (S) messageDetails
            );
        }
    }
}
