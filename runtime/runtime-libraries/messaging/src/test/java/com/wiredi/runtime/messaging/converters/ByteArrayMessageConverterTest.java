package com.wiredi.runtime.messaging.converters;

import com.wiredi.runtime.messaging.Message;
import com.wiredi.runtime.messaging.MessageDetails;
import com.wiredi.runtime.messaging.MessageHeaders;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ByteArrayMessageConverterTest {

    private final ByteArrayMessageConverter converter = new ByteArrayMessageConverter();

    @Test
    void testCanDeserialize() {
        // Arrange
        Message<MessageDetails> message = Message.just("test".getBytes(StandardCharsets.UTF_8));

        // Act & Assert
        assertThat(converter.canDeserialize(message, byte[].class)).isTrue();
        assertThat(converter.canDeserialize(message, String.class)).isFalse();
        assertThat(converter.canDeserialize(message, Integer.class)).isFalse();
    }

    @Test
    void testDeserialize() {
        // Arrange
        byte[] body = "test".getBytes(StandardCharsets.UTF_8);
        Message<MessageDetails> message = Message.just(body);

        // Act
        byte[] result = converter.deserialize(message, byte[].class);

        // Assert
        assertThat(result).isEqualTo(body);
    }

    @Test
    void testCanSerialize() {
        // Arrange
        byte[] payload = "test".getBytes(StandardCharsets.UTF_8);
        MessageHeaders headers = new MessageHeaders();
        MessageDetails details = MessageDetails.NONE;

        // Act & Assert
        assertThat(converter.canSerialize(payload, headers, details)).isTrue();
        assertThat(converter.canSerialize("test", headers, details)).isFalse();
        assertThat(converter.canSerialize(123, headers, details)).isFalse();
    }

    @Test
    void testSerialize() {
        // Arrange
        byte[] payload = "test".getBytes(StandardCharsets.UTF_8);
        MessageHeaders headers = new MessageHeaders.Builder()
                .add("header1", "value1")
                .build();
        MessageDetails details = MessageDetails.NONE;

        // Act
        Message<MessageDetails> result = converter.serialize(payload, headers, details);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.body()).isEqualTo(payload);
        assertThat(result.headers()).isEqualTo(headers);
        assertThat(result.details()).isEqualTo(details);
    }
}