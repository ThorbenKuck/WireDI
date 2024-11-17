package com.wiredi.runtime.messaging;

import com.wiredi.runtime.WireRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageConverterAutoConfigurationTest {

    @Test
    public void testStringConversion() {
        // Arrange
        WireRepository wireRepository = WireRepository.open();
        MessagingEngine messageConverters = wireRepository.get(MessagingEngine.class);

        // Act
        Message<MessageDetails> serialized = messageConverters.serialize("test");

        // Assert
        assertThat(serialized.body()).isEqualTo("test".getBytes());
    }

}