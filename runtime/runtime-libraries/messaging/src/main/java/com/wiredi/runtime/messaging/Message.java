package com.wiredi.runtime.messaging;

import com.wiredi.runtime.lang.ThrowingConsumer;
import com.wiredi.runtime.lang.ThrowingFunction;
import com.wiredi.runtime.messaging.errors.MissingMessageDetailsException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A message is a generic representation of any I/O operation.
 * <p>
 * It is used to transport different kinds of data, containing a body, headers and details.
 * <p>
 * The body is the concrete received payload.
 * Inbound messages are commonly raw messages, containing a byte array as their body.
 * During deserialization, they are converted to a concrete type, and during serialization a Message containing a
 * concrete type will be converted to a Message containing a byte array.
 * <p>
 * The headers are a generic application of common header values, similar to http or messaging headers.
 * These can be used as a general representation of headers anywhere.
 * <p>
 * The details are optional.
 * Integrations can specify their own detail subtypes.
 * Additionally, deserializers can use these details to restrict if when or how a message is deserialized.
 * <p>
 * Messages are stateful instances and should not be modified.
 * Instead, they should be treated as immutable objects and new instances of the Message should be constructed.
 * They are short-lived and only used for transportation of data.
 * <p>
 * Additionally, messages should not be stored for long times and references to them should be reduced as much as possible.
 * To facilitate this, {@link #equals(Object)} and {@link #hashCode()} of a message are not overwritten.
 *
 * @param <T> the generic type of the body
 * @param <S> the subtype of the {@link MessageDetails}
 * @see Headers
 * @see MessageDetails
 */
public class Message<T, S extends MessageDetails> {

    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    @NotNull
    private final Headers headers;
    @NotNull
    private final T body;
    @Nullable
    private final S messageDetails;

    public Message(
            @NotNull Headers headers,
            @NotNull T body,
            @Nullable S messageDetails
    ) {
        this.headers = headers;
        this.body = body;
        this.messageDetails = messageDetails;
    }

    /**
     * Creates a new Builder for a message based on the {@code body}
     *
     * @param body the body that should be contained in the message.
     * @param <T>  the generic type of the body
     * @param <S>  the generic type of the {@link MessageDetails} contained in the Message
     * @return a new {@link Builder} to construct the Message
     */
    public static <T, S extends MessageDetails> Message.Builder<T, S> of(T body) {
        return new Builder<>(body);
    }

    /**
     * Creates a new Builder for a message without any previous details.
     *
     * @param <T> the generic type of the body
     * @param <S> the generic type of the {@link MessageDetails} contained in the Message
     * @return a new {@link Builder} to construct the Message
     */
    public static <T, S extends MessageDetails> Message.Builder<T, S> build() {
        return new Builder<>(null);
    }

    /**
     * Quickly construct a new message with just a body.
     * <p>
     * The resulting message will have no {@link Headers} and no {@link MessageDetails}.
     * Using the mapping functions, these can later be added.
     * <p>
     * However, if {@link Headers} and/or {@link MessageDetails} are known during message construction, consider using
     * the {@link #of(Object)} method to reduce the objects that are being created.
     *
     * @param body the body to wrap in the {@link Message}
     * @param <T>  the generic of the body
     * @return a new {@link Message} containing only the {@code body}
     * @see #of(Object)
     */
    public static <T> Message<T, MessageDetails> just(T body) {
        return new Builder<>(body).build();
    }

    /**
     * Returns the message headers.
     *
     * @return the headers set for this message.
     */
    public @NotNull Headers getHeaders() {
        return headers;
    }

    /**
     * Returns all values for the specified {@code headerName}.
     *
     * @param headerName the header name
     * @return a list of all values for the header name
     * @see Headers#allValues(String)
     */
    @NotNull
    public List<HeaderEntry> getHeaders(@NotNull String headerName) {
        return headers.allValues(headerName);
    }

    /**
     * Returns the last header value for the {@code headerName}, or null if no header exists with the {@code headerName}
     *
     * @param headerName the name of the header
     * @return the last header value for the {@code headerName}, or null if no header with the name is present.
     * @see Headers#lastValue(String)
     */
    @Nullable
    public HeaderEntry getLastHeader(@NotNull String headerName) {
        return headers.lastValue(headerName);
    }

    /**
     * Returns the first header value for the {@code headerName}, or null if no header exists with the {@code headerName}
     *
     * @param headerName the name of the header
     * @return the first header value for the {@code headerName}, or null if no header with the name is present.
     * @see Headers#firstValue(String)
     */
    @Nullable
    public HeaderEntry getFirstHeader(@NotNull String headerName) {
        return headers.lastValue(headerName);
    }

    /**
     * Returns the body of this message.
     *
     * @return the body of this message.
     */
    public @NotNull T getBody() {
        return body;
    }

    /**
     * Returns the details of this message.
     *
     * @return the details of this message.
     */
    public @Nullable S getDetails() {
        return messageDetails;
    }

    /**
     * Returns the details of this message, or throws an exception if the details are missing.
     *
     * @return the details of this message.
     * @throws MissingMessageDetailsException if this message has no details
     */
    public @NotNull S requireDetails() throws MissingMessageDetailsException {
        if (messageDetails == null) {
            throw new MissingMessageDetailsException(this);
        }
        return messageDetails;
    }

    /**
     * Returns the details of this message, or throws an exception if the details are missing.
     *
     * @param errorMessageSupplier the error message to pass to the {@link MissingMessageDetailsException} if no details are present
     * @return the details of this message.
     * @throws MissingMessageDetailsException if this message has no details
     */
    public @NotNull S requireDetails(Supplier<String> errorMessageSupplier) throws MissingMessageDetailsException {
        if (messageDetails == null) {
            throw new MissingMessageDetailsException(this, errorMessageSupplier.get());
        }
        return messageDetails;
    }

    /**
     * Maps the body of this message and constructs a new instance of the {@link Message}.
     * <p>
     * The references of the {@link Headers} and {@link MessageDetails} in the new message will be the same as in this one.
     *
     * @param mapper      the mapping function to map the body.
     * @param <D>         The generic type of the mapped body.
     * @param <THROWABLE> (Optional) a generic of any throwable that the mapping function can throw.
     * @return a new message containing the mapped body.
     * @throws THROWABLE if the {@link ThrowingFunction} throws the provided throwable
     * @see ThrowingFunction
     */
    public <D, THROWABLE extends Throwable> @NotNull Message<D, S> map(@NotNull ThrowingFunction<@NotNull T, @NotNull D, THROWABLE> mapper) throws THROWABLE {
        return new Message<>(
                headers,
                mapper.apply(body),
                messageDetails
        );
    }

    /**
     * Maps the details of this message and constructs a new instance of the {@link Message}.
     * <p>
     * The references of the {@code body} and {@link Headers} in the new message will be the same as in this one.
     *
     * @param mapper      the mapping function to map the body.
     * @param <D>         The generic type of the mapped body.
     * @param <THROWABLE> (Optional) a generic of any throwable that the mapping function can throw.
     * @return a new message containing the mapped body.
     * @throws THROWABLE if the {@link ThrowingFunction} throws the provided throwable
     * @see ThrowingFunction
     */
    public <D extends MessageDetails, THROWABLE extends Throwable> @NotNull Message<T, D> mapDetails(@NotNull ThrowingFunction<@Nullable S, @NotNull D, THROWABLE> mapper) throws THROWABLE {
        return new Message<>(
                headers,
                body,
                mapper.apply(messageDetails)
        );
    }

    /**
     * Maps the headers of this message and constructs a new instance of the {@link Message}.
     * <p>
     * The references of the {@code body} and {@link MessageDetails} in the new message will be the same as in this one.
     *
     * @param constructor the consumer to modify the headers
     * @param <THROWABLE> (Optional) a generic of any throwable that the mapping function can throw.
     * @return a new message containing the mapped body.
     * @throws THROWABLE if the {@link ThrowingFunction} throws the provided throwable
     * @see ThrowingConsumer
     */
    public <THROWABLE extends Throwable> @NotNull Message<T, S> mapHeaders(@NotNull ThrowingConsumer<@NotNull Headers, THROWABLE> constructor) throws THROWABLE {
        Headers copy = headers.copy();
        constructor.accept(copy);
        return new Message<>(
                copy,
                body,
                messageDetails
        );
    }

    @Override
    public String toString() {
        List<String> fieldValues = new ArrayList<>();
        fieldValues.add("body=" + body);
        if (!headers.isEmpty()) {
            fieldValues.add("headers=" + headers);
        }
        if (messageDetails != null) {
            fieldValues.add("messageDetails=" + messageDetails);
        }
        return "Message{" +
                String.join(", ", fieldValues) +
                '}';
    }

    public static class Builder<T, S extends MessageDetails> {

        private final Headers.Builder headers = Headers.builder();
        @Nullable
        private T body;
        @Nullable
        private MessageDetails messageDetails;

        public Builder(@Nullable T body) {
            this.body = body;
        }

        @NotNull
        public T body() {
            if (body == null) {
                throw new IllegalStateException("Tried to build a message without a body");
            }
            return body;
        }

        @NotNull
        public Headers.Builder headers() {
            return headers;
        }

        @NotNull
        public Builder<T, S> withBody(@NotNull T body) {
            this.body = body;
            return this;
        }

        @NotNull
        public Builder<T, S> addHeader(String key, String value) {
            headers.add(key, value);
            return this;
        }

        @NotNull
        public Builder<T, S> addHeaders(Headers headers) {
            this.headers.addAll(headers);
            return this;
        }

        @NotNull
        public <D extends MessageDetails> Builder<T, D> withDetails(@NotNull D details) {
            this.messageDetails = details;
            return (Builder<T, D>) this;
        }

        @NotNull
        public Message<T, S> build() {
            return new Message<>(
                    headers.build(),
                    body(),
                    (S) messageDetails
            );
        }
    }
}
