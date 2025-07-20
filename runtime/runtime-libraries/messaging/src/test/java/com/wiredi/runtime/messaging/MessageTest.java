package com.wiredi.runtime.messaging;

import com.wiredi.runtime.messaging.messages.InputStreamMessage;
import com.wiredi.runtime.messaging.messages.SimpleMessage;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class MessageTest {

    @Test
    void testBuilderWithByteArray() {
        // Arrange
        byte[] body = "test".getBytes(StandardCharsets.UTF_8);

        // Act
        Message<MessageDetails> message = Message.builder(body)
                .addHeader("header1", "value1")
                .build();

        // Assert
        assertThat(message).isInstanceOf(SimpleMessage.class);
        assertThat(message.body()).isEqualTo(body);
        assertThat(message.headers().isEmpty()).isFalse();
        assertThat(message.headers("header1")).hasSize(1);
        assertThat(message.header("header1").decodeToString()).isEqualTo("value1");
        assertThat(message.details()).isEqualTo(MessageDetails.NONE);
    }

    @Test
    void testBuilderWithInputStream() {
        // Arrange
        byte[] body = "test".getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(body);

        // Act
        Message<MessageDetails> message = Message.builder(inputStream)
                .addHeader("header1", "value1")
                .build();

        // Assert
        assertThat(message).isInstanceOf(InputStreamMessage.class);
        assertThat(message.body()).isEqualTo(body);
        assertThat(message.headers().isEmpty()).isFalse();
        assertThat(message.headers("header1")).hasSize(1);
        assertThat(message.header("header1").decodeToString()).isEqualTo("value1");
        assertThat(message.details()).isEqualTo(MessageDetails.NONE);
    }

    @Test
    void testNewEmptyMessage() {
        // Act
        Message<MessageDetails> message = Message.newEmptyMessage().build();

        // Assert
        assertThat(message.body()).isEmpty();
        assertThat(message.headers()).isNotNull();
        assertThat(message.headers().isEmpty()).isTrue();
        assertThat(message.details()).isEqualTo(MessageDetails.NONE);
    }

    @Test
    void testEmpty() {
        // Act
        Message<MessageDetails> message = Message.empty();

        // Assert
        assertThat(message.body()).isEmpty();
        assertThat(message.headers()).isNotNull();
        assertThat(message.headers().isEmpty()).isTrue();
        assertThat(message.details()).isEqualTo(MessageDetails.NONE);
    }

    @Test
    void testJustWithBody() {
        // Arrange
        byte[] body = "test".getBytes(StandardCharsets.UTF_8);

        // Act
        Message<MessageDetails> message = Message.just(body);

        // Assert
        assertThat(message.body()).isEqualTo(body);
        assertThat(message.headers()).isNotNull();
        assertThat(message.headers().isEmpty()).isTrue();
        assertThat(message.details()).isEqualTo(MessageDetails.NONE);
    }

    @Test
    void testJustWithBodyAndHeaders() {
        // Arrange
        byte[] body = "test".getBytes(StandardCharsets.UTF_8);
        MessageHeaders headers = new MessageHeaders.Builder()
                .add("header1", "value1")
                .build();

        // Act
        Message<MessageDetails> message = Message.just(body, headers);

        // Assert
        assertThat(message.body()).isEqualTo(body);
        assertThat(message.headers()).isEqualTo(headers);
        assertThat(message.details()).isEqualTo(MessageDetails.NONE);
    }

    @Test
    void testJustWithBodyAndDetails() {
        // Arrange
        byte[] body = "test".getBytes(StandardCharsets.UTF_8);
        MessageDetails details = new TestMessageDetails();

        // Act
        Message<MessageDetails> message = Message.just(body, details);

        // Assert
        assertThat(message.body()).isEqualTo(body);
        assertThat(message.headers()).isNotNull();
        assertThat(message.headers().isEmpty()).isTrue();
        assertThat(message.details()).isEqualTo(details);
    }

    @Test
    void testJustWithBodyHeadersAndDetails() {
        // Arrange
        byte[] body = "test".getBytes(StandardCharsets.UTF_8);
        MessageHeaders headers = new MessageHeaders.Builder()
                .add("header1", "value1")
                .build();
        MessageDetails details = new TestMessageDetails();

        // Act
        Message<MessageDetails> message = Message.just(body, headers, details);

        // Assert
        assertThat(message.body()).isEqualTo(body);
        assertThat(message.headers()).isEqualTo(headers);
        assertThat(message.details()).isEqualTo(details);
    }

    @Test
    void testJustWithInputStream() {
        // Arrange
        byte[] body = "test".getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(body);

        // Act
        Message<MessageDetails> message = Message.just(inputStream);

        // Assert
        assertThat(message).isInstanceOf(InputStreamMessage.class);
        assertThat(message.body()).isEqualTo(body);
        assertThat(message.headers()).isNotNull();
        assertThat(message.headers().isEmpty()).isTrue();
        assertThat(message.details()).isEqualTo(MessageDetails.NONE);
    }

    @Test
    void testHeaderAccessMethods() {
        // Arrange
        byte[] body = "test".getBytes(StandardCharsets.UTF_8);
        MessageHeader header1 = MessageHeader.of("header1", "value1");
        MessageHeader header2 = MessageHeader.of("header2", "value2");
        MessageHeader header3 = MessageHeader.of("header1", "value3");

        Message<MessageDetails> message = Message.builder(body)
                .addHeader(header1)
                .addHeader(header2)
                .addHeader(header3)
                .build();

        // Act & Assert
        assertThat(message.headers("header1")).hasSize(2);
        assertThat(message.headers("header1")).containsExactly(header1, header3);

        assertThat(message.header("header1")).isEqualTo(header3);
        assertThat(message.getLastHeader("header1")).isEqualTo(header3);
        assertThat(message.getFirstHeader("header1")).isEqualTo(header1);

        assertThat(message.header("nonexistent")).isNull();
    }

    @Test
    void testBuffer() {
        // Arrange
        byte[] body = "test".getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(body);
        Message<MessageDetails> message = Message.just(inputStream);

        // Act
        Message<MessageDetails> bufferedMessage = message.buffer();

        // Assert
        assertThat(bufferedMessage).isInstanceOf(SimpleMessage.class);
        assertThat(bufferedMessage.body()).isEqualTo(body);
    }

    @Test
    void testBodySize() {
        // Arrange
        byte[] body = "test".getBytes(StandardCharsets.UTF_8);
        Message<MessageDetails> message = Message.just(body);

        // Act & Assert
        assertThat(message.bodySize()).isEqualTo(body.length);
    }

    @Test
    void testHasDetails() {
        // Arrange
        byte[] body = "test".getBytes(StandardCharsets.UTF_8);
        Message<MessageDetails> messageWithoutDetails = Message.just(body);
        Message<MessageDetails> messageWithDetails = Message.just(body, new TestMessageDetails());

        // Act & Assert
        assertThat(messageWithoutDetails.hasDetails()).isFalse();
        assertThat(messageWithDetails.hasDetails()).isTrue();
    }

    @Test
    void testWriteBodyTo() throws IOException {
        // Arrange
        byte[] body = "test".getBytes(StandardCharsets.UTF_8);
        Message<MessageDetails> message = Message.just(body);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Act
        message.writeBodyTo(outputStream);

        // Assert
        assertThat(outputStream.toByteArray()).isEqualTo(body);
    }

    @Test
    void testInputStream() throws IOException {
        // Arrange
        byte[] body = "test".getBytes(StandardCharsets.UTF_8);
        Message<MessageDetails> message = Message.just(body);

        // Act
        InputStream inputStream = message.inputStream();
        byte[] readBody = inputStream.readAllBytes();

        // Assert
        assertThat(readBody).isEqualTo(body);
    }

    @Test
    void testCopyWithPayload() {
        // Arrange
        byte[] body1 = "test1".getBytes(StandardCharsets.UTF_8);
        byte[] body2 = "test2".getBytes(StandardCharsets.UTF_8);
        MessageHeaders headers = new MessageHeaders.Builder()
                .add("header1", "value1")
                .build();
        MessageDetails details = new TestMessageDetails();
        Message<MessageDetails> message = Message.just(body1, headers, details);

        // Act
        Message<MessageDetails> copiedMessage = message.copyWithPayload(body2);

        // Assert
        assertThat(copiedMessage.body()).isEqualTo(body2);
        assertThat(copiedMessage.headers()).isEqualTo(headers);
        assertThat(copiedMessage.details()).isEqualTo(details);
    }

    @Test
    void testCopyWithPayloadInputStream() {
        // Arrange
        byte[] body1 = "test1".getBytes(StandardCharsets.UTF_8);
        byte[] body2 = "test2".getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(body2);
        MessageHeaders headers = new MessageHeaders.Builder()
                .add("header1", "value1")
                .build();
        MessageDetails details = new TestMessageDetails();
        Message<MessageDetails> message = Message.just(body1, headers, details);

        // Act
        Message<MessageDetails> copiedMessage = message.copyWithPayload(inputStream);

        // Assert
        assertThat(copiedMessage.body()).isEqualTo(body2);
        assertThat(copiedMessage.headers()).isEqualTo(headers);
        assertThat(copiedMessage.details()).isEqualTo(details);
    }

    @Test
    void testChunkedState() {
        // Arrange
        byte[] body = "test".getBytes(StandardCharsets.UTF_8);
        Message<MessageDetails> message = Message.just(body);

        // Act & Assert
        assertThat(message.isChunked()).isFalse();

        message.setChunked(true);
        assertThat(message.isChunked()).isTrue();

        message.setChunked(false);
        assertThat(message.isChunked()).isFalse();
    }

    private static class TestMessageDetails implements MessageDetails {
        @Override
        public boolean isNotNone() {
            return true;
        }
    }
}
