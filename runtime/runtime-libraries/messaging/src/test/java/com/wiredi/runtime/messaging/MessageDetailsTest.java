package com.wiredi.runtime.messaging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageDetailsTest {

    @Test
    void testNoneInstance() {
        // Act & Assert
        assertThat(MessageDetails.NONE).isNotNull();
        assertThat(MessageDetails.NONE.isNone()).isTrue();
        assertThat(MessageDetails.NONE.isNotNone()).isFalse();
    }

    @Test
    void testCustomImplementation() {
        // Arrange
        MessageDetails customDetails = new TestMessageDetails();

        // Act & Assert
        assertThat(customDetails.isNone()).isFalse();
        assertThat(customDetails.isNotNone()).isTrue();
    }

    private static class TestMessageDetails implements MessageDetails {
        // Empty implementation
    }
}