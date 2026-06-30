package com.wiredi.health;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HealthNodeTest {

    @Test
    void shouldSetCreatedStatus_whenConstructedWithDefaultConstructor() {
        // Arrange
        // Act
        HealthNode node = new HealthNode();
        // Assert
        assertThat(node.status()).as("Default status should be CREATED").isEqualTo(HealthStatus.CREATED);
    }

    @Test
    void shouldHaveEmptyUnmodifiableDetails_whenConstructedWithDefaultConstructor() {
        // Arrange
        // Act
        HealthNode node = new HealthNode();
        // Assert
        assertThat(node.details()).as("Default details should be empty").isEmpty();
        assertThatThrownBy(() -> node.details().put("k", "v"))
                .as("Default details map should be unmodifiable")
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldSetProvidedStatus_whenConstructedWithStatus() {
        // Arrange
        // Act
        HealthNode node = new HealthNode(HealthStatus.UP);
        // Assert
        assertThat(node.status()).as("Status should be set from constructor").isEqualTo(HealthStatus.UP);
        assertThat(node.details()).as("Details should default to empty map when only status is provided").isEmpty();
    }

    @Test
    void shouldSetProvidedStatusAndDetails_whenConstructedWithStatusAndDetails() {
        // Arrange
        Map<String, String> details = new HashMap<>();
        details.put("a", "1");
        details.put("b", "2");
        // Act
        HealthNode node = new HealthNode(HealthStatus.DOWN, details);
        // Assert
        assertThat(node.status()).as("Status should be set from constructor").isEqualTo(HealthStatus.DOWN);
        assertThat(node.details()).as("Details should be set from constructor").containsEntry("a", "1").containsEntry("b", "2");
    }

    @Test
    void shouldReflectExternalMapChanges_whenConstructedWithStatusAndDetails() {
        // Arrange
        Map<String, String> details = new HashMap<>();
        details.put("a", "1");
        HealthNode node = new HealthNode(HealthStatus.STARTING, details);
        // Act
        details.put("b", "2"); // underlying unmodifiable view should reflect this change
        // Assert
        assertThat(node.details()).containsEntry("a", "1").containsEntry("b", "2");
    }

    @Test
    void shouldThrowOnDetailsMutation_whenAttemptingToModifyDetails() {
        // Arrange
        Map<String, String> details = new HashMap<>();
        details.put("a", "1");
        HealthNode node = new HealthNode(HealthStatus.STARTING, details);
        // Act + Assert
        assertThatThrownBy(() -> node.details().put("c", "3"))
                .as("Details map is unmodifiable")
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldAllowChangingStatusViaSetter_whenSetStatusIsInvoked() {
        // Arrange
        HealthNode node = new HealthNode();
        // Act
        node.setStatus(HealthStatus.UP);
        // Assert
        assertThat(node.status()).isEqualTo(HealthStatus.UP);
        // Act
        node.setStatus(HealthStatus.STOPPING);
        // Assert
        assertThat(node.status()).isEqualTo(HealthStatus.STOPPING);
    }
}
