package com.wiredi.runtime.messaging;

import com.wiredi.runtime.lang.ThrowingConsumer;
import com.wiredi.runtime.lang.ThrowingFunction;
import com.wiredi.runtime.messaging.errors.MissingMessageConverterException;
import com.wiredi.runtime.messaging.messages.SimpleMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An interface that aggregates {@link MessageConverter} instance and handle (de)serializations.
 * <p>
 * Implementations aggregate {@link MessageConverter} implementations that are invoked when {@link #serialize(Message)}
 * and {@link #deserialize(Message, Class)} are invoked.
 * Strategies on how {@link MessageConverter} instances are asked for (de)serialization and how they are
 * resolved are dependent on implementations of this interface.
 * <p>
 * This class is intended to be the primary interface to use when considering message conversion.
 * Integrations of any kind should use this class exclusively and not rely on the underlying {@link MessageConverter}
 * instances directly.
 *
 * @see CompositeMessagingEngine
 * @see MessageConverter
 * @see SimpleMessage
 */
public interface MessagingEngine {

    static MessagingEngine of(
            MessagingContext messageEngineContext,
            RequestContext requestContext
    ) {
        return new CompositeMessagingEngine(messageEngineContext, requestContext);
    }

    static MessagingEngine of(MessagingContext context) {
        return new CompositeMessagingEngine(context);
    }

    static MessagingEngine of(RequestContext requestContext) {
        return new CompositeMessagingEngine(requestContext);
    }

    static MessagingEngine defaultEngine() {
        return new CompositeMessagingEngine();
    }

    /**
     * Processes the provided {@link Message}.
     * <p>
     * When processing the message within the {@code handler}, the message provided to the Function should be used,
     * as {@link RequestAware} instances may alter the message.
     * <p>
     * The returned result is describing what happened during processing.
     * Underlying implementations should use the {@link RequestContext} to process the message.
     *
     * @param message the received {@link Message} that should be processed
     * @param handler the handler that handles the (potentially modified) {@link Message}
     * @param <E>     potential errors that could be thrown while applying the handler
     * @param <D>     the generic of the {@link MessageDetails} that the provided message has.
     * @return a {@link MessagingResult} of processing the {@link Message}
     * @see RequestContext
     * @see #handleMessage(Message, ThrowingConsumer)
     * @see #handleMessage(byte[], MessageHeaders, MessageDetails, ThrowingConsumer)
     * @see #handleMessage(byte[], MessageDetails, ThrowingConsumer)
     * @see #handleMessage(byte[], MessageHeaders, ThrowingConsumer)
     * @see #handleMessage(byte[], ThrowingConsumer)
     */
    <E extends Throwable, D extends MessageDetails> MessagingResult processMessage(Message<D> message, ThrowingFunction<Message<D>, Object, E> handler);

    default <E extends Throwable, S extends MessageDetails> MessagingResult handleMessage(Message<S> message, ThrowingConsumer<Message<S>, E> handler) {
        return processMessage(message, b -> {
            handler.accept(b);
            return null;
        });
    }

    default <E extends Throwable, S extends MessageDetails> MessagingResult handleMessage(
            byte[] body,
            @NotNull MessageHeaders headers,
            @NotNull S messageDetails,
            ThrowingConsumer<Message<S>, E> handler
    ) throws E {
        return handleMessage(
                Message.builder(body)
                        .withDetails(messageDetails)
                        .addHeaders(headers)
                        .build(),
                handler
        );
    }

    default <E extends Throwable, S extends MessageDetails> MessagingResult handleMessage(
            byte[] body,
            @NotNull S messageDetails,
            ThrowingConsumer<Message<S>, E> handler
    ) throws E {
        return handleMessage(body, new MessageHeaders(), messageDetails, handler);
    }

    default <E extends Throwable> MessagingResult handleMessage(
            byte[] body,
            @NotNull MessageHeaders headers,
            ThrowingConsumer<Message<MessageDetails>, E> handler
    ) throws E {
        return handleMessage(body, headers, MessageDetails.NONE, handler);
    }

    default <E extends Throwable> MessagingResult handleMessage(byte[] body, ThrowingConsumer<Message<MessageDetails>, E> handler) throws E {
        return handleMessage(body, new MessageHeaders(), MessageDetails.NONE, handler);
    }

    /**
     * Deserialize the provided {@code rawMessage} to a concrete {@link Message} of type {@code targetType}.
     * <p>
     * Implementation should never return non-null instances.
     * Instead, they should consider throwing an Exception corresponding to the error that prevented message conversion.
     *
     * @param rawMessage the raw message that should be deserialized
     * @param targetType the type that the deserialized message should contain
     * @param <T>        the generic type of the Message, based on the {@code targetType}
     * @param <S>        the generic MessageDetails, already contained in the {@code rawMessage}
     * @return a new, deserialized {@link Message} with the type {@code targetType}
     * @throws MissingMessageConverterException if no {@link MessageConverter} was found to deserialize the {@code rawMessage} to the {@code targetType}
     */
    @NotNull
    <T, S extends MessageDetails> T deserialize(
            @NotNull Message<S> rawMessage,
            @NotNull Class<T> targetType
    ) throws MissingMessageConverterException;

    /**
     * Deserialize the provided {@code messageBuilder} to a concrete {@link SimpleMessage} of type {@code targetType}.
     * <p>
     * Implementation should never return non-null instances.
     * Instead, they should consider throwing an Exception corresponding to the error that prevented message conversion.
     *
     * @param messageBuilder the raw message that should be deserialized
     * @param targetType     the type that the deserialized message should contain
     * @param <T>            the generic type of the Message, based on the {@code targetType}
     * @param <S>            the generic MessageDetails, already contained in the {@code messageBuilder}
     * @return a new, deserialized {@link SimpleMessage} with the type {@code targetType}
     * @throws MissingMessageConverterException if no {@link MessageConverter} was found to deserialize the {@code messageBuilder} to the {@code targetType}
     */
    @NotNull
    default <T, S extends MessageDetails> T deserialize(
            @NotNull SimpleMessage.Builder<S> messageBuilder,
            @NotNull Class<T> targetType
    ) throws MissingMessageConverterException {
        return deserialize(messageBuilder.build(), targetType);
    }

    /**
     * Serialize the provided {@code message} to a raw message.
     * <p>
     * Implementation should never return non-null instances.
     * Instead, they should consider throwing an Exception corresponding to the error that prevented message conversion.
     *
     * @param payload the raw message that should be deserialized
     * @param <S>     the generic MessageDetails, already contained in the {@code message}
     * @return a new, serialized {@link SimpleMessage}
     * @throws MissingMessageConverterException if no {@link MessageConverter} was found to serialize the {@code message}
     */
    @NotNull
    <S extends MessageDetails> Message<S> serialize(@Nullable Object payload, @NotNull MessageHeaders headers, @NotNull S details) throws MissingMessageConverterException;

    @NotNull
    default Message<MessageDetails> serialize(@Nullable Object payload, @NotNull MessageHeaders headers) throws MissingMessageConverterException {
        return serialize(payload, headers, MessageDetails.NONE);
    }

    @NotNull
    default <S extends MessageDetails> Message<S> serialize(@Nullable Object payload, @NotNull S details) throws MissingMessageConverterException {
        return serialize(payload, new MessageHeaders(), details);
    }

    @NotNull
    default Message<MessageDetails> serialize(@Nullable Object payload) throws MissingMessageConverterException {
        return serialize(payload, null, MessageDetails.NONE);
    }

    @NotNull MessagingContext messageEngineContext();

    @NotNull RequestContext requestContext();
}
