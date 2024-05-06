package com.wiredi.tests.async;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;


class AsyncResultTest {

    @Test
    public void testList() throws InterruptedException {
        // Arrange
        AsyncResultList<String> list = AsyncResult.list();
        Thread thread = new Thread(() -> {
            list.operate(() -> {
                list.add("test 1");
                list.add("test 2");
            });
        });

        // Act
        list.prime(2);
        thread.start();
        thread.join();

        // Assert
        assertThat(list.isCompleted()).isTrue();
        assertThat(list.get()).containsExactly("test 1", "test 2");
    }

    @Test
    public void errorsArePropagated() throws InterruptedException {
        // Arrange
        AsyncResultList<String> list = AsyncResult.list();
        Throwable exception = new IllegalStateException("Test exception");
        Thread thread = new Thread(() -> {
            list.operate(() -> {
                list.add("test 1");
                throw exception;
            });
        });

        // Act
        list.prime(2);
        thread.start();
        thread.join();

        // Assert
        assertThat(list.isCompleted()).isFalse();
        assertThatCode(list::get)
                .isInstanceOf(AssertionFailedError.class)
                .hasCause(exception);
    }
}