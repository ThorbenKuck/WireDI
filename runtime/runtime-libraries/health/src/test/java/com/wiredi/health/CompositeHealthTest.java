package com.wiredi.health;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompositeHealthTest {

    @Test
    void shouldDefaultToCreatedAndEmptyDetails_whenConstructedWithDefaultConstructor() {
        // Arrange
        // Act
        CompositeHealth health = new CompositeHealth();

        // Assert
        assertThat(health.status()).isEqualTo(HealthStatus.CREATED);
        assertThat(health.details()).isEmpty();
        assertThatThrownBy(() -> health.details().put("x", "y"))
                .as("Details map must be unmodifiable")
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldUseProvidedStatus_whenConstructedWithStatus() {
        // Arrange
        // Act
        CompositeHealth health = new CompositeHealth(HealthStatus.UP);

        // Assert
        assertThat(health.status()).isEqualTo(HealthStatus.UP);
        assertThat(health.details()).isEmpty();
    }

    @Test
    void shouldUseProvidedStatusAndDetails_whenConstructedWithStatusAndDetails() {
        // Arrange
        Map<String, String> details = new HashMap<>();
        details.put("k1", "v1");

        // Act
        CompositeHealth health = new CompositeHealth(HealthStatus.DOWN, details);

        // Assert
        assertThat(health.status()).isEqualTo(HealthStatus.DOWN);
        assertThat(health.details()).containsEntry("k1", "v1");
    }

    @Test
    void shouldReflectExternalMapChanges_whenConstructedWithStatusAndDetails() {
        // Arrange
        Map<String, String> details = new HashMap<>();
        details.put("k1", "v1");
        CompositeHealth health = new CompositeHealth(HealthStatus.STARTING, details);

        // Act
        details.put("k2", "v2");

        // Assert
        assertThat(health.details()).containsEntry("k1", "v1").containsEntry("k2", "v2");
    }

    @Test
    void shouldAllowStatusMutationViaSetter_whenSetStatusIsInvoked() {
        // Arrange
        CompositeHealth health = new CompositeHealth();
        // Act
        health.setStatus(HealthStatus.UP);
        // Assert
        assertThat(health.status()).isEqualTo(HealthStatus.UP);
        // Act
        health.setStatus(HealthStatus.STOPPING);
        // Assert
        assertThat(health.status()).isEqualTo(HealthStatus.STOPPING);
    }

    @Test
    void shouldAddModuleAndReturnNull_whenAddingNewModuleHealth() {
        // Arrange
        CompositeHealth health = new CompositeHealth();
        HealthNode module = new HealthNode(HealthStatus.UP);

        // Act
        Health previous = health.addModuleHealth("moduleA", module);

        // Assert
        assertThat(previous).as("No previous health should be registered").isNull();
        // add same again to ensure retrieval via replacement behavior
        Health old = health.addModuleHealth("moduleA", new HealthNode(HealthStatus.DOWN));
        assertThat(old).as("Previous health should be returned on overwrite").isEqualTo(module);
    }

    @Test
    void shouldReturnPreviousHealth_whenAddingModuleWithExistingName() {
        // Arrange
        CompositeHealth health = new CompositeHealth();
        HealthNode first = new HealthNode(HealthStatus.UP);
        HealthNode second = new HealthNode(HealthStatus.DOWN);
        health.addModuleHealth("service", first);

        // Act
        Health previous = health.addModuleHealth("service", second);

        // Assert
        assertThat(previous).isSameAs(first);
    }

    @Test
    void shouldAddModuleUsingNamedHealth_andReturnPrevious() {
        // Arrange
        CompositeHealth health = new CompositeHealth();
        HealthNode first = new HealthNode(HealthStatus.UP);
        HealthNode second = new HealthNode(HealthStatus.DOWN);

        // Act
        Health previousFirst = health.addModuleHealth(new NamedHealth("component", first));
        Health previousSecond = health.addModuleHealth(new NamedHealth("component", second));

        // Assert
        assertThat(previousFirst).as("First insert should return null").isNull();
        assertThat(previousSecond).as("Second insert should return the prior health").isSameAs(first);
    }

    @Test
    void shouldSupportHealthNamedHelper_whenAddingViaNamedHealth() {
        // Arrange
        CompositeHealth health = new CompositeHealth();
        Health first = new HealthNode(HealthStatus.UP);
        Health second = new HealthNode(HealthStatus.FAULTY);

        // Act
        Health prev1 = health.addModuleHealth(first.named("db"));
        Health prev2 = health.addModuleHealth(second.named("db"));

        // Assert
        assertThat(prev1).isNull();
        assertThat(prev2).isSameAs(first);
    }
}
