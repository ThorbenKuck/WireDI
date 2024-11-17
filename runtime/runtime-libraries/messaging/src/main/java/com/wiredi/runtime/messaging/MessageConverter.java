package com.wiredi.runtime.messaging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An interface for message conversion.
 * <p>
 * This interface is used inside the {@link MessagingEngine} implementations to (de)serialize messages, with
 * {@link Message} being an abstraction for messaging implementations.
 * <p>
 * Both serialized and deserialized instances are represented as a Message.
 * Serialized instances are messages containing raw byte-arrays.
 * <p>
 * Instances should consider to use {@link com.wiredi.annotations.Order} or {@link com.wiredi.runtime.domain.Ordered}
 * to define executions.
 * If multiple converters can (de)serialize messages, the first that is found will be asked to convert.
 * Ordering the converters allows for control over which converter will be asked first
 *
 * @see Message
 * @see MessagingEngine
 * @see com.wiredi.annotations.Order
 * @see com.wiredi.runtime.domain.Ordered
 */
public interface MessageConverter<T, S extends MessageDetails> {

    /**
     * Whether this instance can deserialize the provided {@code message} to the {@code targetType}.
     * <p>
     * If true is returned, this converter will be asked to deserialize the message using {@link #deserialize(Message, Class)}.
     *
     * @param message    the message to deserialize
     * @param targetType the type that the deserialized message should have
     * @return true, if this converter can deserialize the message
     */
    boolean canDeserialize(@NotNull Message<?> message, @NotNull Class<?> targetType);

    /**
     * Deserialize the provided {@code message} to the {@code targetType}.
     * <p>
     * This method will only be invoked if {@link #canDeserialize(Message, Class)} returns true.
     * If this function returns a non-null value, no other converter will be asked to deserialize the {@code message}.
     * If null is returned, the next potential converter will be asked to deserialize the {@code message}.
     *
     * @param message    the message to deserialize.
     * @param targetType the type that the resulting message should contain.
     * @return a deserialized message, or null if deserialization was not possible.
     */
    @Nullable
    T deserialize(@NotNull Message<S> message, @NotNull Class<T> targetType);

    /**
     * Whether this instance can serialize the provided {@code payload}.
     * <p>
     * If true is returned, this converter will be asked to serialize the payload using {@link #serialize(Object, MessageHeaders, MessageDetails)}.
     *
     * @param payload the payload to serialize
     * @return true, if this converter can serialize the payload
     */
    boolean canSerialize(@NotNull Object payload, @NotNull MessageHeaders headers, @NotNull MessageDetails messageDetails);

    /**
     * Serialize the provided {@code payload}.
     * <p>
     * This method will only be invoked if {@link #canSerialize(Object, MessageHeaders, MessageDetails)} returns true.
     * If this function returns a non-null value, no other converter will be asked to serialize the {@code payload}.
     * If null is returned, the next potential converter will be asked to serialize the {@code payload}.
     *
     * @param payload the payload to serialize.
     * @return a serialized payload, or null if serialization was not possible.
     */
    @Nullable
    Message<S> serialize(@NotNull Object payload, @NotNull MessageHeaders headers, @NotNull S messageDetails);

}
