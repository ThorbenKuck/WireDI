package com.wiredi.examples;

import com.wiredi.runtime.WireContainer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class BaseTest {

    @Test
    public void orderingWorks() {
        // Arrange
        WireContainer wireRepository = WireContainer.open();

        // Act
        List<Base> bases = wireRepository.getAll(Base.class);

        // Assert
        assertThat(bases).containsExactly(
                wireRepository.get(First.class),
                wireRepository.get(Second.class),
                wireRepository.get(Third.class),
                wireRepository.get(Fourth.class),
                wireRepository.get(Fifth.class)
        );
    }
}