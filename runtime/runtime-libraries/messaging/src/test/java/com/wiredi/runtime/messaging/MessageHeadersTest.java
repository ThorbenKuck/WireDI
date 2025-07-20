package com.wiredi.runtime.messaging;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class MessageHeadersTest {

    @Test
    void testEmptyHeaders() {
        // Act
        MessageHeaders headers = new MessageHeaders();

        // Assert
        assertThat(headers.isEmpty()).isTrue();
        assertThat(headers.map()).isEmpty();
        assertThat(headers.allValues("any")).isEmpty();
        assertThat(headers.firstValue("any")).isNull();
        assertThat(headers.lastValue("any")).isNull();
    }

    @Test
    void testConstantEmptyHeaders() {
        // Act & Assert
        assertThat(MessageHeaders.EMPTY.isEmpty()).isTrue();
        assertThat(MessageHeaders.EMPTY.map()).isEmpty();
    }

    @Test
    void testConstructorWithMap() {
        // Arrange
        Map<String, List<MessageHeader>> map = new HashMap<>();
        List<MessageHeader> values = new ArrayList<>();
        values.add(MessageHeader.of("test", "value1"));
        values.add(MessageHeader.of("test", "value2"));
        map.put("test", values);

        // Act
        MessageHeaders headers = new MessageHeaders(map);

        // Assert
        assertThat(headers.isEmpty()).isFalse();
        assertThat(headers.allValues("test")).hasSize(2);
        assertThat(headers.firstValue("test").decodeToString()).isEqualTo("value1");
        assertThat(headers.lastValue("test").decodeToString()).isEqualTo("value2");
    }

    @Test
    void testStaticFactoryMethodOfWithMap() {
        // Arrange
        Map<String, List<String>> map = new HashMap<>();
        map.put("test", List.of("value1", "value2"));

        // Act
        MessageHeaders headers = MessageHeaders.of(map);

        // Assert
        assertThat(headers.isEmpty()).isFalse();
        assertThat(headers.allValues("test")).hasSize(2);
        assertThat(headers.firstValue("test").decodeToString()).isEqualTo("value1");
        assertThat(headers.lastValue("test").decodeToString()).isEqualTo("value2");
    }

    @Test
    void testStaticFactoryMethodOfWithIterable() {
        // Arrange
        List<MessageHeader> headerList = List.of(
                MessageHeader.of("test1", "value1"),
                MessageHeader.of("test2", "value2"),
                MessageHeader.of("test1", "value3")
        );

        // Act
        MessageHeaders headers = MessageHeaders.of(headerList);

        // Assert
        assertThat(headers.isEmpty()).isFalse();
        assertThat(headers.allValues("test1")).hasSize(2);
        assertThat(headers.allValues("test2")).hasSize(1);
        assertThat(headers.firstValue("test1").decodeToString()).isEqualTo("value1");
        assertThat(headers.lastValue("test1").decodeToString()).isEqualTo("value3");
    }

    @Test
    void testBuilderMethods() {
        // Act
        MessageHeaders headers = MessageHeaders.builder()
                .add("header1", "value1")
                .add("header2", "value2")
                .add("header1", "value3")
                .build();

        // Assert
        assertThat(headers.isEmpty()).isFalse();
        assertThat(headers.allValues("header1")).hasSize(2);
        assertThat(headers.allValues("header2")).hasSize(1);
        assertThat(headers.firstValue("header1").decodeToString()).isEqualTo("value1");
        assertThat(headers.lastValue("header1").decodeToString()).isEqualTo("value3");
    }

    @Test
    void testBuilderWithExistingHeaders() {
        // Arrange
        MessageHeaders original = MessageHeaders.builder()
                .add("header1", "value1")
                .add("header2", "value2")
                .build();

        // Act
        MessageHeaders copy = MessageHeaders.builder(original)
                .add("header3", "value3")
                .build();

        // Assert
        assertThat(copy.allValues("header1")).hasSize(1);
        assertThat(copy.allValues("header2")).hasSize(1);
        assertThat(copy.allValues("header3")).hasSize(1);
    }

    @Test
    void testCopyMethod() {
        // Arrange
        MessageHeaders original = MessageHeaders.builder()
                .add("header1", "value1")
                .build();

        // Act
        MessageHeaders copy = original.copy().add("header2", "value2").build();

        // Assert
        assertThat(copy.allValues("header1")).hasSize(1);
        assertThat(copy.allValues("header2")).hasSize(1);
        assertThat(original.allValues("header2")).isEmpty();
    }

    @Test
    void testBuilderSetMethods() {
        // Act
        MessageHeaders headers = MessageHeaders.builder()
                .add("header1", "value1")
                .add("header1", "value2")
                .set("header1", "value3")
                .build();

        // Assert
        assertThat(headers.allValues("header1")).hasSize(1);
        assertThat(headers.firstValue("header1").decodeToString()).isEqualTo("value3");
    }

    @Test
    void testBuilderAddWithByteArray() {
        // Arrange
        byte[] value = "test".getBytes(StandardCharsets.UTF_8);

        // Act
        MessageHeaders headers = MessageHeaders.builder()
                .add("header1", value)
                .build();

        // Assert
        assertThat(headers.firstValue("header1").content()).isEqualTo(value);
    }

    @Test
    void testBuilderSetWithByteArray() {
        // Arrange
        byte[] value = "test".getBytes(StandardCharsets.UTF_8);

        // Act
        MessageHeaders headers = MessageHeaders.builder()
                .set("header1", value)
                .build();

        // Assert
        assertThat(headers.firstValue("header1").content()).isEqualTo(value);
    }

    @Test
    void testBuilderAddAllWithCollection() {
        // Arrange
        List<MessageHeader> headerList = List.of(
                MessageHeader.of("test", "value1"),
                MessageHeader.of("test", "value2")
        );

        // Act
        MessageHeaders headers = MessageHeaders.builder()
                .addAll("test", headerList)
                .build();

        // Assert
        assertThat(headers.allValues("test")).hasSize(2);
    }

    @Test
    void testBuilderClear() {
        // Act
        MessageHeaders headers = MessageHeaders.builder()
                .add("header1", "value1")
                .clear()
                .add("header2", "value2")
                .build();

        // Assert
        assertThat(headers.allValues("header1")).isEmpty();
        assertThat(headers.allValues("header2")).hasSize(1);
    }

    @Test
    void testBuilderSnapshot() {
        // Arrange
        MessageHeaders.Builder builder = MessageHeaders.builder()
                .add("header1", "value1")
                .add("header2", "value2");

        // Act
        Map<String, List<MessageHeader>> snapshot = builder.snapshot();

        // Assert
        assertThat(snapshot).hasSize(2);
        assertThat(snapshot.get("header1")).hasSize(1);
        assertThat(snapshot.get("header2")).hasSize(1);
    }

    @Test
    void testForEach() {
        // Arrange
        MessageHeaders headers = MessageHeaders.builder()
                .add("header1", "value1")
                .add("header2", "value2")
                .build();
        Map<String, List<MessageHeader>> collected = new HashMap<>();

        // Act
        headers.forEach(collected::put);

        // Assert
        assertThat(collected).hasSize(2);
        assertThat(collected.get("header1")).hasSize(1);
        assertThat(collected.get("header2")).hasSize(1);
    }

    @Test
    void testIterator() {
        // Arrange
        MessageHeader header1 = MessageHeader.of("header1", "value1");
        MessageHeader header2 = MessageHeader.of("header2", "value2");
        MessageHeader header3 = MessageHeader.of("header1", "value3");
        
        MessageHeaders headers = MessageHeaders.builder()
                .add(header1)
                .add(header2)
                .add(header3)
                .build();
        
        // Act
        List<MessageHeader> collected = new ArrayList<>();
        for (MessageHeader header : headers) {
            collected.add(header);
        }

        // Assert
        assertThat(collected).hasSize(3);
        assertThat(collected).contains(header1, header2, header3);
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        MessageHeaders headers1 = MessageHeaders.builder()
                .add("header1", "value1")
                .add("header2", "value2")
                .build();
        
        MessageHeaders headers2 = MessageHeaders.builder()
                .add("header1", "value1")
                .add("header2", "value2")
                .build();
        
        MessageHeaders headers3 = MessageHeaders.builder()
                .add("header1", "value1")
                .add("header3", "value3")
                .build();

        // Assert
        assertThat(headers1).isEqualTo(headers2);
        assertThat(headers1.hashCode()).isEqualTo(headers2.hashCode());
        
        assertThat(headers1).isNotEqualTo(headers3);
        assertThat(headers1).isNotEqualTo(null);
        assertThat(headers1).isNotEqualTo("not headers");
    }

    @Test
    void testToString() {
        // Arrange
        MessageHeaders headers = MessageHeaders.builder()
                .add("header1", "value1")
                .build();

        // Act
        String result = headers.toString();

        // Assert
        assertThat(result).contains("MessageHeaders");
        assertThat(result).contains("header1");
    }
}