package com.wiredi.examples;

import com.wiredi.runtime.WireContainer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class BaseTest {

    @Test
    public void orderingWorks() {
        // Arrange
        WireContainer wireContainer = WireContainer.open();

        // Act
        List<Base> bases = wireContainer.getAll(Base.class);

        // Assert
        assertThat(bases).containsExactly(
                wireContainer.get(First.class),
                wireContainer.get(Second.class),
                wireContainer.get(Third.class),
                wireContainer.get(Fourth.class),
                wireContainer.get(Fifth.class)
        );
    }
}