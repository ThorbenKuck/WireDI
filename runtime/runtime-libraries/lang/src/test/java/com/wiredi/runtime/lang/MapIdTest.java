package com.wiredi.runtime.lang;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MapIdTest {

    @Test
    public void theMapIdIsResolvedIndependentOfItsInstance() {
        // Arrange
        Map<MapId, String> map = new HashMap<>();

        // Act
        map.put(MapId.of(1).add("1"), "test");

        // Assert
        assertThat(map.get(MapId.of(1).add("1"))).isEqualTo("test");
    }

    @Test
    public void theOrderOfArgumentsIsRelevant() {
        // Arrange
        Map<MapId, String> map = new HashMap<>();

        // Act
        map.put(MapId.of("1").add(1), "test");

        // Assert
        assertThat(map.get(MapId.of(1).add("1"))).isEqualTo(null);
    }
}