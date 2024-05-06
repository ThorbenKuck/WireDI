package com.wiredi.runtime.messaging;

import com.wiredi.runtime.WireRepository;
import org.junit.jupiter.api.Test;

class MessageConverterAutoConfigurationTest {

    @Test
    public void testStringConversion() {
        // Arrange
        WireRepository wireRepository = WireRepository.open();
        MessagingEngine messageConverters = wireRepository.get(MessagingEngine.class);

        // Act
        Message<byte[], MessageDetails> serialized = messageConverters.serialize(Message.of("test").build());

        // Assert
    }

}