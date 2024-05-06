package com.wiredi.runtime.properties.loader;

import com.wiredi.runtime.properties.Key;
import com.wiredi.runtime.resources.builtin.ClassPathResource;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class YamlPropertyFileTypeLoaderTest {

    private static final YamlPropertyFileTypeLoader loader = new YamlPropertyFileTypeLoader();

    @Test
    public void testThatLoadingAYamlFileWorks() {
        // Arrange
        Map<Key, String> expected = Map.of(
                Key.just("my.test"), "property1,property2",
                Key.just("my.bar.buf"), "bim,bam"
        );

        // Act
        Map<Key, String> actual = loader.extract(new ClassPathResource("test.yaml"));

        // Assert
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testThatLoadingAFlatYamlFileWorks() {
        // Arrange

        Map<Key, String> expected = Map.of(
                Key.just("foo.bar.baz"), "bam",
                Key.just("foo.bar.buz.buy"), "buf"
        );

        // Act
        Map<Key, String> actual = loader.extract(new ClassPathResource("test.yml"));

        // Assert
        assertThat(actual).isEqualTo(expected);
    }
}
