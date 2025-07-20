package com.wiredi.runtime.messaging.converters;

import com.wiredi.runtime.messaging.Message;
import com.wiredi.runtime.messaging.MessageDetails;
import com.wiredi.runtime.messaging.MessageHeaders;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class StringMessageConverterTest {

    private final StringMessageConverter converter = new StringMessageConverter();

    @Test
    void testCanDeserialize() {
        // Arrange
        Message<MessageDetails> message = Message.just("test".getBytes(StandardCharsets.UTF_8));

        // Act & Assert
        assertThat(converter.canDeserialize(message, String.class)).isTrue();
        assertThat(converter.canDeserialize(message, byte[].class)).isFalse();
        assertThat(converter.canDeserialize(message, Integer.class)).isFalse();
    }

    @Test
    void testDeserialize() {
        // Arrange
        String expected = "test";
        Message<MessageDetails> message = Message.just(expected.getBytes(StandardCharsets.UTF_8));

        // Act
        String result = converter.deserialize(message, String.class);

        // Assert
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void testCanSerialize() {
        // Arrange
        String payload = "test";
        MessageHeaders headers = new MessageHeaders();
        MessageDetails details = MessageDetails.NONE;

        // Act & Assert
        assertThat(converter.canSerialize(payload, headers, details)).isTrue();
        assertThat(converter.canSerialize(payload.getBytes(StandardCharsets.UTF_8), headers, details)).isFalse();
        assertThat(converter.canSerialize(123, headers, details)).isFalse();
    }

    @Test
    void testSerialize() {
        // Arrange
        String payload = "test";
        MessageHeaders headers = new MessageHeaders.Builder()
                .add("header1", "value1")
                .build();
        MessageDetails details = MessageDetails.NONE;

        // Act
        Message<MessageDetails> result = converter.serialize(payload, headers, details);

        // Assert
        assertThat(result).isNotNull();
        assertThat(new String(result.body(), StandardCharsets.UTF_8)).isEqualTo(payload);
        assertThat(result.headers()).isEqualTo(headers);
        assertThat(result.details()).isEqualTo(details);
    }
}