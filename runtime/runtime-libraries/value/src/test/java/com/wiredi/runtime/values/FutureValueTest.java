package com.wiredi.runtime.values;

import com.wiredi.runtime.values.FutureValue;
import com.wiredi.runtime.values.Value;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class FutureValueTest {

    @Test
    public void whenSettingAValueGetReturnsTheValue() {
        // Arrange
        Value<@NotNull String> value = FutureValue.of("Test");
        assertThat(value.get()).isEqualTo("Test");

        // Act
        value.set("Test2");

        // Assert
        assertThat(value.get()).isEqualTo("Test2");
    }

    @Test
    public void theAsyncValueIsResolvedAsync() throws InterruptedException {
        // Arrange
        FutureValue<@NotNull String> value = FutureValue.of(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "Test";
        });

        // Act
        String result = value.get(Duration.ofSeconds(2));

        // Assert
        assertThat(result).isEqualTo("Test");
    }

}