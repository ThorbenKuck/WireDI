package com.wiredi.test;

import com.wiredi.runtime.WireContainer;
import com.wiredi.tests.ApplicationTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * A simple test to demonstrate how to test WireDI functionality.
 */
@ApplicationTest
public class SimpleWireRepositoryTest {

    private final WireContainer wireContainer;

    public SimpleWireRepositoryTest(WireContainer wireContainer) {
        this.wireContainer = wireContainer;
    }

    @Test
    public void testWireRepositoryCreation() {
        // Arrange
        // Act
        // Assert
        assertThat(wireContainer).isNotNull();
        System.out.println("[DEBUG_LOG] WireRepository created successfully");
    }

    @Test
    public void testGetExistingBean() {
        // Arrange
        // Act
        CountInvocationsAspect aspect = wireContainer.get(CountInvocationsAspect.class);

        // Assert
        assertThat(aspect).isNotNull();
        System.out.println("[DEBUG_LOG] Successfully retrieved CountInvocationsAspect bean");
    }
}