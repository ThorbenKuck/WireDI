package com.wiredi.runtime.properties;

import com.wiredi.runtime.properties.loader.PropertiesPropertyFileTypeLoader;
import com.wiredi.runtime.properties.loader.YamlPropertyFileTypeLoader;
import com.wiredi.runtime.resources.builtin.ClassPathResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PropertyLoaderTest {

    @Test
    public void testThatYamlFilesCanBeLoaded() {
        // Arrange
        String file = "test.yaml";
        String property = "my.test";
        String expectedValue = "property1,property2";
        PropertyLoader propertyLoader = new PropertyLoader(new YamlPropertyFileTypeLoader());

        // Act
        TypedProperties properties = Assertions.assertDoesNotThrow(() -> propertyLoader.load(new ClassPathResource(file)));

        // Assert
        assertThat(properties.get(Key.just(property)))
                .withFailMessage("Property was missing")
                .isPresent()
                .withFailMessage("Property had the wrong value")
                .hasValue(expectedValue);
    }

    @Test
    public void testThatYmlFilesCanBeLoaded() {
        // Arrange
        String file = "test.yml";
        PropertyLoader propertyLoader = new PropertyLoader(new YamlPropertyFileTypeLoader());

        // Act
        TypedProperties properties = Assertions.assertDoesNotThrow(() -> propertyLoader.load(new ClassPathResource(file)));

        // Assert
        assertThat(properties.get(Key.just("foo.bar.baz")))
                .withFailMessage("Property was missing")
                .isPresent()
                .withFailMessage("Property had the wrong value")
                .hasValue("bam");
        assertThat(properties.get(Key.just("foo.bar.buz.buy")))
                .withFailMessage("Property was missing")
                .isPresent()
                .withFailMessage("Property had the wrong value")
                .hasValue("buf");
    }

    @Test
    public void testThatPropertiesFilesCanBeLoaded() {
        // Arrange
        String file = "test.properties";
        String property = "my.test";
        String expectedValue = "property1,property2";
        PropertyLoader propertyLoader = new PropertyLoader(new PropertiesPropertyFileTypeLoader());

        // Act
        TypedProperties properties = Assertions.assertDoesNotThrow(() -> propertyLoader.load(new ClassPathResource(file)));

        // Assert
        assertThat(properties.get(Key.just(property)))
                .withFailMessage("Property was missing")
                .isPresent()
                .withFailMessage("Property had the wrong value")
                .hasValue(expectedValue);
    }
}
