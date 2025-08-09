package com.wiredi.test;

import com.wiredi.test.properties.ExampleProperties;
import com.wiredi.test.properties.SeparateProperties;
import com.wiredi.tests.ApplicationTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ApplicationTest
public class PropertyLoadingTest {

    @Test
    public void testThatSeparatePropertiesAreLoadedCorrectly(SeparateProperties separateProperties) {
        // Arrange Act Assert
        assertThat(separateProperties.foo()).isNotNull().isEqualTo("baz");
        assertThat(separateProperties.pi()).isNotNull().isEqualTo(3);
    }

    @Test
    public void testThatExamplePropertiesAreLoadedCorrectly(ExampleProperties separateProperties) {
        // Arrange Act Assert
        assertThat(separateProperties.getFoo()).isNotNull().isEqualTo("bar");
        assertThat(separateProperties.getPi()).isNotNull().isEqualTo(0.0);
    }

}
