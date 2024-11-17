package com.wiredi.runtime.messaging;

import com.wiredi.runtime.messaging.converters.ByteArrayMessageConverter;
import com.wiredi.runtime.messaging.converters.StringMessageConverter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class MessagingEngineTest {

    @Test
    public void test() {
        // Arrange
        MessagingEngine engine = MessagingEngine.of(
                new MessagingContext(
                        List.of(new ByteArrayMessageConverter(), new StringMessageConverter()),
                        List.of()
                )
        );
        String payload = "test";
        Message<MessageDetails> message = Message.just(payload.getBytes());

        // Act
        MessagingResult result = engine.handleMessage(message, m -> {
            assertThat(m.body()).isEqualTo(payload.getBytes());
        });

        // Assert
        assertThat(result).isInstanceOf(MessagingResult.Success.class);
    }

    @Test
    public void errorsInTheHandlingYieldTheResult() {
        // Arrange
        RuntimeException error = new RuntimeException("test");
        MessagingEngine engine = MessagingEngine.of(
                new MessagingContext(
                        List.of(new ByteArrayMessageConverter(), new StringMessageConverter()),
                        List.of()
                )
        );
        String payload = "test";
        Message<MessageDetails> message = Message.just(payload.getBytes());

        // Act
        MessagingResult result = engine.handleMessage(message, m -> {
            assertThat(m.body()).isEqualTo(payload.getBytes());
            throw error;
        });

        // Assert
        assertThat(result)
                .isEqualTo(new MessagingResult.Failed(error));
    }
}
