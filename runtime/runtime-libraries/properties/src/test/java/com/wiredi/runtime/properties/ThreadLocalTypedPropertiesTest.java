package com.wiredi.runtime.properties;

import com.wiredi.runtime.properties.keys.PreFormattedKey;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class ThreadLocalTypedPropertiesTest {

    @Test
    public void emptyTypedPropertiesWillBeEmptyForEachThread() throws InterruptedException {
        // Arrange
        ThreadLocalTypedProperties threadLocal = new ThreadLocalTypedProperties();
        PreFormattedKey key = Key.just("test");
        AtomicBoolean wasMissing = new AtomicBoolean();
        Thread thread = new Thread(() -> {
            Optional<String> testValue = threadLocal.get().get(key);
            wasMissing.set(testValue.isEmpty());
        });

        // Act
        threadLocal.get().set(key, "test");
        thread.start();
        thread.join();

        // Assert
        assertTrue(wasMissing.get());
    }

    @Test
    public void prefilledTypedPropertiesWillHaveTheInitialValueForEachThread() throws InterruptedException {
        // Arrange
        PreFormattedKey key = Key.just("test");
        String expectedValue = "test";
        ThreadLocalTypedProperties threadLocal = new ThreadLocalTypedProperties(
                new TypedProperties().set(key, expectedValue)
        );
        AtomicBoolean wasTest = new AtomicBoolean();
        Thread thread = new Thread(() -> {
            threadLocal.get()
                    .get(key)
                    .ifPresent(actualValue -> wasTest.set(actualValue.equals(expectedValue)));
        });

        // Act
        threadLocal.get().set(key, "not-" + expectedValue);
        thread.start();
        thread.join();

        // Assert
        assertTrue(wasTest.get());
    }

}