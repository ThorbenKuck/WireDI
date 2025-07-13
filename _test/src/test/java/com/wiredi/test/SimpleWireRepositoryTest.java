package com.wiredi.test;

import com.wiredi.runtime.WireContainer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * A simple test to demonstrate how to test WireDI functionality.
 */
public class SimpleWireRepositoryTest extends AbstractIntegrationTest {

    @Test
    public void testWireRepositoryCreation() {
        // Arrange & Act
        WireContainer wireRepository = loadWireRepository();

        // Assert
        assertThat(wireRepository).isNotNull();
        System.out.println("[DEBUG_LOG] WireRepository created successfully");
    }

    @Test
    public void testGetExistingBean() {
        // Arrange
        WireContainer wireRepository = loadWireRepository();

        // Act
        CountInvocationsAspect aspect = wireRepository.get(CountInvocationsAspect.class);

        // Assert
        assertThat(aspect).isNotNull();
        assertThat(aspect.invocations()).isEqualTo(0);
        System.out.println("[DEBUG_LOG] Successfully retrieved CountInvocationsAspect bean");
    }
}