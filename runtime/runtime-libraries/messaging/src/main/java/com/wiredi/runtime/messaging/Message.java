package com.wiredi.runtime.messaging;

import com.wiredi.runtime.messaging.messages.InputStreamMessage;
import com.wiredi.runtime.messaging.messages.SimpleMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * A message is a generic representation of any I/O data packet.
 * <p>
 * It is used to transport different kinds of data, containing a body, headers and details.
 * <p>
 * Each message hast to have a body.
 * This body is the concrete-received payload.
 * Implementations of this interface can determine how the body is received.
 * <p>
 * Inbound messages are commonly raw messages, containing a byte array as their body.
 * During deserialization, The Message is converted to a concrete type, and during serialization a Message containing
 * the serialized body is constructed.
 * <p>
 * Additionally to the body, each Message has to have {@link MessageHeaders}.
 * These headers are a generic application of common header values, similar to http or messaging headers.
 * In incoming messages the {@link MessageHeaders} are headers received, while {@link MessageHeaders} in outbound
 * messages are to be transported to the receiving system.
 * <p>
 * Lastly, a Message must have {@link MessageDetails}.
 * {@link MessageDetails} contain details of the Message source.
 * Different technologies can provide different implementations of {@link MessageDetails}.
 * During processing of messages, the {@link MessageDetails} can then be used to differentiate between technologies
 * or to get the context of the message.
 * If a message has no details, it contains the {@link MessageDetails#NONE}
 * <p>
 * Messages are stateful instances and are not modifiable.
 * They should be treated as immutable objects, and new instances of the Message should be constructed if fields should
 * be changed.
 * They are short-lived and only used for transportation of data.
 * <p>
 * Additionally, messages should not be stored for long times and references to them should not be stored.
 * To facilitate this, {@link Object#equals(Object)} and {@link Object#hashCode()} of a message should NOT be overwritten.
 *
 * @param <D> the subtype of the {@link MessageDetails}
 * @see MessageHeaders
 * @see MessageDetails
 */
public interface Message<D extends MessageDetails> {

    byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /**
     * Creates a new Builder for a message based on the {@code body}
     *
     * @param body the body that should be contained in the message.
     * @param <D>  the generic type of the {@link MessageDetails} contained in the Message
     * @return a new {@link SimpleMessage.Builder} to construct the Message
     */
    static <D extends MessageDetails> SimpleMessage.Builder<D> builder(byte[] body) {
        return new SimpleMessage.Builder<>(body);
    }

    /**
     * Starts a new builder, wrapping the provided input stream as the body.
     * <p>
     * This method constructs a new {@link InputStreamMessage}.
     * For details see the implementation.
     *
     * @param inputStream the {@link InputStream} to wrap
     * @param <D>         the generic type pf the {@link MessageDetails} contained in the message.
     * @return a new Builder to build the {@link InputStreamMessage}
     * @see InputStreamMessage
     */
    static <D extends MessageDetails> InputStreamMessage.Builder<D> builder(InputStream inputStream) {
        return new InputStreamMessage.Builder<>(inputStream);
    }

    /**
     * Creates a new Builder for a message based on the {@code body}
     *
     * @param <D> the generic type of the {@link MessageDetails} contained in the Message
     * @return a new {@link SimpleMessage.Builder} to construct the Message
     */
    static <D extends MessageDetails> SimpleMessage.Builder<D> newEmptyMessage() {
        return new SimpleMessage.Builder<>(EMPTY_BYTE_ARRAY);
    }

    /**
     * Creates a new simply empty Message.
     *
     * @return a new {@link SimpleMessage} with no content
     */
    static SimpleMessage<MessageDetails> empty() {
        return new SimpleMessage<>(EMPTY_BYTE_ARRAY, new MessageHeaders(), MessageDetails.NONE);
    }

    /**
     * Quickly construct a new message with just a body.
     * <p>
     * The resulting message will have no {@link MessageHeaders} and no {@link MessageDetails}.
     *
     * @param body the body to wrap in the {@link SimpleMessage}
     * @return a new {@link SimpleMessage} containing only the {@code body}
     * @see #builder(byte[])
     */
    static Message<MessageDetails> just(byte[] body) {
        return new SimpleMessage<>(body, new MessageHeaders(), MessageDetails.NONE);
    }

    /**
     * Quickly construct a new message with just a body.
     * <p>
     * The resulting message will have no {@link MessageHeaders} and no {@link MessageDetails}.
     * Headers can be modified later.
     * <p>
     * However, if {@link MessageHeaders} and/or {@link MessageDetails} are known during message construction, consider using
     * the {@link #builder(byte[])} method to reduce the objects that are being created.
     *
     * @param body the body to wrap in the {@link SimpleMessage}
     * @return a new {@link SimpleMessage} containing only the {@code body}
     * @see #builder(byte[])
     */
    static Message<MessageDetails> just(byte[] body, @NotNull MessageHeaders headers) {
        return new SimpleMessage<>(body, headers, MessageDetails.NONE);
    }

    /**
     * Quickly construct a new message with just a body.
     * <p>
     * The resulting message will have no {@link MessageHeaders} and no {@link MessageDetails}.
     * Headers can be modified later.
     * <p>
     * However, if {@link MessageHeaders} and/or {@link MessageDetails} are known during message construction, consider using
     * the {@link #builder(byte[])} method to reduce the objects that are being created.
     *
     * @param body the body to wrap in the {@link SimpleMessage}
     * @return a new {@link SimpleMessage} containing only the {@code body}
     * @see #builder(byte[])
     */
    static Message<MessageDetails> just(byte[] body, @NotNull MessageDetails details) {
        return new SimpleMessage<>(body, new MessageHeaders(), details);
    }

    /**
     * Quickly construct a new message with just a body.
     * <p>
     * The resulting message will have no {@link MessageHeaders} and no {@link MessageDetails}.
     * Headers can be modified later.
     * <p>
     * However, if {@link MessageHeaders} and/or {@link MessageDetails} are known during message construction, consider using
     * the {@link #builder(byte[])} method to reduce the objects that are being created.
     *
     * @param body the body to wrap in the {@link SimpleMessage}
     * @return a new {@link SimpleMessage} containing only the {@code body}
     * @see #builder(byte[])
     */
    static Message<MessageDetails> just(
            byte[] body,
            @NotNull MessageHeaders headers,
            @NotNull MessageDetails details
    ) {
        return new SimpleMessage<>(body, headers, details);
    }

    /**
     * Quickly construct a new message with just a body.
     * <p>
     * The resulting message will have no {@link MessageHeaders} and no {@link MessageDetails}.
     * Headers can be modified later.
     * <p>
     * However, if {@link MessageHeaders} and/or {@link MessageDetails} are known during message construction, consider using
     * the {@link #builder(byte[])} method to reduce the objects that are being created.
     *
     * @param inputStream the body to wrap in the {@link SimpleMessage}
     * @return a new {@link SimpleMessage} containing only the {@code body}
     * @see #builder(byte[])
     */
    @NotNull
    static Message<MessageDetails> just(@NotNull InputStream inputStream) {
        return new InputStreamMessage<>(inputStream, new MessageHeaders(), MessageDetails.NONE, true);
    }

    /**
     * Any Headers associated with this message.
     *
     * @return the headers
     */
    @NotNull MessageHeaders headers();

    /**
     * A utility function to find all headers for a name.
     *
     * @param headerName the name of the header
     * @return all {@link MessageHeaders} that match the name.
     * @see MessageHeaders#allValues(String)
     */
    default @NotNull List<MessageHeader> headers(@NotNull String headerName) {
        return headers().allValues(headerName);
    }

    /**
     * A utility function to find a header for a name.
     * <p>
     * This method is synonymous to {@link #getLastHeader(String)}.
     *
     * @param headerName the name of the header
     * @return the {@link MessageHeaders} that match the name.
     * @see MessageHeaders#allValues(String)
     */
    default @Nullable MessageHeader header(@NotNull String headerName) {
        return headers().lastValue(headerName);
    }

    /**
     * A utility function to find the last header for a name.
     *
     * @param headerName the name of the header
     * @return the last {@link MessageHeaders} that match the name.
     * @see MessageHeaders#lastValue(String)
     */
    default @Nullable MessageHeader getLastHeader(@NotNull String headerName) {
        return headers().lastValue(headerName);
    }

    /**
     * A utility function to find the first header for a name.
     *
     * @param headerName the name of the header
     * @return the first {@link MessageHeaders} that match the name.
     * @see MessageHeaders#firstValue(String)
     */
    default @Nullable MessageHeader getFirstHeader(@NotNull String headerName) {
        return headers().firstValue(headerName);
    }

    /**
     * Constructs a new message which allows for repeated ready of the body.
     * <p>
     * Though this allows for repeated reads, keep in mind that big Messages generally wrap an InputStream to not read
     * all bytes to memory.
     * This method may counteract the intention behind using the input stream.
     *
     * @return a new Message instance that contains the same content as this one.
     */
    @NotNull
    default Message<D> buffer() {
        return new SimpleMessage<>(
                body(),
                headers(),
                details()
        );
    }

    /**
     * The body of this message.
     * <p>
     * A message body can also be understood as "Payload".
     * <p>
     * In the case of Messages that build on InputStreams, this method my throw an {@link UncheckedIOException} if the
     * input stream cannot be read.
     * This maybe because of the IO input is closed, or because of repeated reads.
     * Because of this, it is generally recommended to call this method only once.
     * <p>
     * If repeated calls are required, please consider using the {@link #buffer} method to construct a Message.
     *
     * @return return raw message body
     * @throws UncheckedIOException if the underlying implementation can no longer return the body
     * @see #inputStream()
     * @see #buffer()
     */
    byte[] body() throws UncheckedIOException;

    /**
     * The size of the body.
     * <p>
     * In the case of simple messages, this method can be resolved on demand.
     * <p>
     * If the method returns a negative value, the body size is considered unknown.
     *
     * @return the size of {@link #body()}
     */
    long bodySize();

    /**
     * The details of this message.
     * <p>
     * Details are helpful to determine the origins of a message and to control deserialization.
     * They also help by providing detailed information about the source.
     * <p>
     * In the case that a Message has no Details (which is a valid scenario), this method should return {@link MessageDetails#NONE}.
     *
     * @return the details of this message
     */
    @NotNull D details();

    default boolean hasDetails() {
        return details().isNotNone();
    }

    /**
     * Writes the body of this message to the provided {@code outputStream}.
     * <p>
     * Implementations can decide if this implementation is repeatable or not.
     * As InputStreams may throw errors on repeated throws, this method may as well.
     * <p>
     * Because of this, it is recommended to consider a message empty once this method is called and to no longer
     * use it.
     *
     * @param outputStream the OutputStream to write to
     * @throws IOException if the underlying message cannot write to the OutputStream
     */
    void writeBodyTo(OutputStream outputStream) throws IOException;

    /**
     * This method returns an input stream to read the content of this Message.
     * <p>
     * There is no guarantee that the underlying {@link InputStream} is not closed.
     *
     * @return the InputStream to read the body
     */
    @NotNull InputStream inputStream();

    Message<D> copyWithPayload(InputStream inputStream);

    Message<D> copyWithPayload(byte[] bytes);

    /**
     * Whether this message is chunked.
     * <p>
     * If true,
     * processing systems should respect the behavior and prioritize using the {@link #inputStream()}} method
     * over directly accessing the {@link #body()}.
     *
     * @return whether this message is chunked.
     */
    boolean isChunked();

    /**
     * Indicate if this Message is chunked.
     *
     * @param chunked the chunked state to set.
     * @return this
     */
    Message<D> setChunked(boolean chunked);
}
