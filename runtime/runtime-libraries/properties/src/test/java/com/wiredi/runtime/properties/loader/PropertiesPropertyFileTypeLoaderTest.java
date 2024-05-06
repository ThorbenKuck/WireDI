package com.wiredi.runtime.properties.loader;

import com.wiredi.runtime.properties.Key;
import com.wiredi.runtime.resources.builtin.ClassPathResource;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PropertiesPropertyFileTypeLoaderTest {

    private static final PropertiesPropertyFileTypeLoader loader = new PropertiesPropertyFileTypeLoader();

    @Test
    public void testThatLoadingAYamlFileWorks() {
        // Arrange
        Map<Key, String> expected = Map.of(
                Key.just("simple"), "simple",
                Key.just("my.bar.buf"), "bim,bam",
                Key.just("my.test"), "property1,property2"
        );

        // Act
        Map<Key, String> actual = loader.extract(new ClassPathResource("test.properties"));

        // Assert
        assertThat(actual).isEqualTo(expected);
    }
}
