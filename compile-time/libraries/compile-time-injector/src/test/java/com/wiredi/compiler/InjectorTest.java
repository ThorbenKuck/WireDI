package com.wiredi.compiler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InjectorTest {

    @Test
    public void theInjectorAlwaysCreatesTheInjectorAsItself() {
        // Arrange
        Injector injector = new Injector();

        // Act
        Injector first = injector.get(Injector.class);
        Injector second = injector.get(Injector.class);

        // Assert
        assertSame(first, second);
    }

}