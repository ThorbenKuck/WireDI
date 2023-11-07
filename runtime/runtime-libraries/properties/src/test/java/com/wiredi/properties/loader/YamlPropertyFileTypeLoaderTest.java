package com.wiredi.properties.loader;

import com.wiredi.properties.keys.Key;
import com.wiredi.resources.builtin.ClassPathResource;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class YamlPropertyFileTypeLoaderTest {

    @Test
    public void testThatLoadingAYamlFileWorks() {
        // Arrange
        YamlPropertyFileTypeLoader loader = new YamlPropertyFileTypeLoader();
        Map<Key, String> expected = Map.of(
                Key.just("my.test"), "property1,property2",
                Key.just("my.bar.buf"), "bim,bam"
        );

        // Act
        Map<Key, String> actual = loader.extract(new ClassPathResource("test.yaml"));

        // Assert
        assertThat(actual).isEqualTo(expected);
    }
}
