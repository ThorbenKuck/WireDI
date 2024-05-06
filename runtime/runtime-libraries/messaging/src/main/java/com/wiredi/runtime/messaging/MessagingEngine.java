package com.wiredi.runtime.messaging;

import com.wiredi.runtime.messaging.errors.MissingMessageConverterException;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

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
 * @see CompositeMessageEngine
 * @see MessageConverter
 * @see Message
 */
public interface MessagingEngine {

    /**
     * Constructs a new MessageConverts instance.
     *
     * @param converters all converters to be used in the CompositeMessageConverters
     * @return a new CompositeMessageConverters instance
     * @see CompositeMessageEngine
     */
    static CompositeMessageEngine of(MessageConverter<?, ?>... converters) {
        return new CompositeMessageEngine(Arrays.asList(converters));
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
    <T, S extends MessageDetails> Message<T, S> deserialize(
            @NotNull Message<byte[], S> rawMessage,
            @NotNull Class<T> targetType
    ) throws MissingMessageConverterException;

    /**
     * Serialize the provided {@code message} to a raw message.
     * <p>
     * Implementation should never return non-null instances.
     * Instead, they should consider throwing an Exception corresponding to the error that prevented message conversion.
     *
     * @param message the raw message that should be deserialized
     * @param <T>     the generic type of the Message, based on the {@code targetType}
     * @param <S>     the generic MessageDetails, already contained in the {@code message}
     * @return a new, serialized {@link Message}
     * @throws MissingMessageConverterException if no {@link MessageConverter} was found to serialize the {@code message}
     */
    @NotNull
    <T, S extends MessageDetails> Message<byte[], S> serialize(@NotNull Message<T, S> message) throws MissingMessageConverterException;

}
