package com.wiredi.runtime.environment;

import com.wiredi.runtime.properties.Key;
import com.wiredi.runtime.Environment;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class EnvironmentTest {

    @Test
    public void testThatResolvingAVariableWorks() {
        // Arrange
        Environment environment = Environment.build();
        String applicationTitle = "Test";
        environment.properties().set(Key.just("application.title"), applicationTitle);

        // Act
        String resolved = environment.resolve("${application.title}");

        // Assert
        assertThat(resolved).isEqualTo(applicationTitle);
    }

    @Test
    public void testThatResolvingAVariableInAStringWorks() {
        // Arrange
        Environment environment = Environment.build();
        String applicationTitle = "Test";
        environment.properties().set(Key.just("application.title"), applicationTitle);

        // Act
        String resolved = environment.resolve("This is a ${application.title}");

        // Assert
        assertThat(resolved).isEqualTo("This is a " + applicationTitle);
    }

}