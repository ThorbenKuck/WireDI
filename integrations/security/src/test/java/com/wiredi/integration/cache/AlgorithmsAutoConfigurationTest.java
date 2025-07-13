package com.wiredi.integration.cache;

import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.security.crypto.Algorithms;
import com.wiredi.runtime.security.crypto.BCryptAlgorithm;
import com.wiredi.runtime.security.crypto.CryptographicAlgorithm;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class AlgorithmsAutoConfigurationTest {
    @Test
    public void testThatAlgorithmsIsWired() {
        // Arrange
        WireContainer wireContainer = WireContainer.open();

        // Act
        // Assert
        assertThat(wireContainer.contains(Algorithms.class)).isTrue();
        assertThat(wireContainer.get(Algorithms.class).encode("test")).isEqualTo("noop:test");
    }

    @Test
    public void ifAnAlgorithmBeanIsPresentItIsUsedAsSystemAlgorithm() {
        // Arrange
        WireContainer wireContainer = WireContainer.create();
        wireContainer.announce(new IdentifiableProvider<BCryptAlgorithm>() {
            @Override
            public @NotNull List<TypeIdentifier<?>> additionalWireTypes() {
                return List.of(TypeIdentifier.just(CryptographicAlgorithm.class));
            }

            @Override
            public @NotNull TypeIdentifier<? super BCryptAlgorithm> type() {
                return TypeIdentifier.of(BCryptAlgorithm.class);
            }

            @Override
            public BCryptAlgorithm get(@NotNull WireContainer wireContainer, @NotNull TypeIdentifier<BCryptAlgorithm> concreteType) {
                return new BCryptAlgorithm();
            }
        });
        wireContainer.load();

        // Act
        // Assert
        assertThat(wireContainer.contains(Algorithms.class)).isTrue();
        assertThat(wireContainer.get(Algorithms.class).encode("test")).startsWith("bcrypt:$2a$12$");
    }

    @Test
    public void testThatAlgorithmsCanBeWiredInADependency() {
        // Arrange
        WireContainer wireContainer = WireContainer.open();

        // Act
        // Assert
        assertThat(wireContainer.contains(Algorithms.class)).isTrue();
        assertThat(wireContainer.get(AlgorithmsDependency.class).algorithms()).isNotNull();
    }
}