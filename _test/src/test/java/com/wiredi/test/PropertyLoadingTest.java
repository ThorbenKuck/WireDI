package com.wiredi.test;

import com.wiredi.runtime.WireContainer;
import com.wiredi.test.properties.ExampleProperties;
import com.wiredi.test.properties.SeparateProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PropertyLoadingTest extends AbstractIntegrationTest {

    @Test
    public void testThatSeparatePropertiesAreLoadedCorrectly()  {
        // Arrange
        WireContainer wireRepository = loadWireRepository();

        // Act
        SeparateProperties separateProperties = wireRepository.get(SeparateProperties.class);

        // Assert
        assertThat(separateProperties.getFoo()).isNotNull().isEqualTo("baz");
        assertThat(separateProperties.getPi()).isNotNull().isEqualTo(0.0);
    }

    @Test
    public void testThatExamplePropertiesAreLoadedCorrectly()  {
        // Arrange
        WireContainer wireRepository = loadWireRepository();

        // Act
        ExampleProperties separateProperties = wireRepository.get(ExampleProperties.class);

        // Assert
        assertThat(separateProperties.getFoo()).isNotNull().isEqualTo("bar");
        assertThat(separateProperties.getPi()).isNotNull().isEqualTo(0.0);
    }

}
