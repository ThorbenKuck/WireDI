package com.wiredi.integration.cache;

import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.security.SecurityContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SecurityContextAutoConfigurationTest {
    @Test
    public void testThatTheSecurityContextIsWired() {
        // Arrange
        WireContainer wireContainer = WireContainer.open();

        // Act
        // Assert
        assertThat(wireContainer.contains(SecurityContext.class)).isTrue();
    }

    @Test
    public void testThatTheSecurityContextCanBeWiredInADependency() {
        // Arrange
        WireContainer wireContainer = WireContainer.open();

        // Act
        // Assert
        assertThat(wireContainer.contains(SecurityContextDependency.class)).isTrue();
        assertThat(wireContainer.get(SecurityContextDependency.class).securityContext()).isNotNull();
    }
}