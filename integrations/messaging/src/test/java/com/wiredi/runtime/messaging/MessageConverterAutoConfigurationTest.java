package com.wiredi.runtime.messaging;

import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.WiredApplication;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageConverterAutoConfigurationTest {

    @Test
    public void testStringConversion() {
        // Arrange
        WireRepository wireRepository = WiredApplication.start().wireRepository();
        MessagingEngine messageConverters = wireRepository.get(MessagingEngine.class);

        // Act
        Message<MessageDetails> serialized = messageConverters.serialize("test");

        // Assert
        assertThat(serialized.body()).isEqualTo("test".getBytes());
    }

}