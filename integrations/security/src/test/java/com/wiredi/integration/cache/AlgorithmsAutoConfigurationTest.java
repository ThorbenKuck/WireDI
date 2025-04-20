package com.wiredi.integration.cache;

import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.WiredApplication;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.security.SecurityContext;
import com.wiredi.runtime.security.crypto.Algorithms;
import com.wiredi.runtime.security.crypto.BCryptAlgorithm;
import com.wiredi.runtime.security.crypto.CryptographicAlgorithm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class AlgorithmsAutoConfigurationTest {
    @Test
    public void testThatAlgorithmsIsWired() {
        // Arrange
        WireRepository wireRepository = WireRepository.open();

        // Act
        // Assert
        assertThat(wireRepository.contains(Algorithms.class)).isTrue();
        assertThat(wireRepository.get(Algorithms.class).encode("test")).isEqualTo("noop:test");
    }

    @Test
    public void ifAnAlgorithmBeanIsPresentItIsUsedAsSystemAlgorithm() {
        // Arrange
        WireRepository wireRepository = WireRepository.create();
        wireRepository.announce(new IdentifiableProvider<BCryptAlgorithm>() {
            @Override
            public @NotNull List<TypeIdentifier<?>> additionalWireTypes() {
                return List.of(TypeIdentifier.just(CryptographicAlgorithm.class));
            }

            @Override
            public @NotNull TypeIdentifier<? super BCryptAlgorithm> type() {
                return TypeIdentifier.of(BCryptAlgorithm.class);
            }

            @Override
            public BCryptAlgorithm get(@NotNull WireRepository wireRepository, @NotNull TypeIdentifier<BCryptAlgorithm> concreteType) {
                return new BCryptAlgorithm();
            }
        });
        wireRepository.load();

        // Act
        // Assert
        assertThat(wireRepository.contains(Algorithms.class)).isTrue();
        assertThat(wireRepository.get(Algorithms.class).encode("test")).startsWith("bcrypt:$2a$12$");
    }

    @Test
    public void testThatAlgorithmsCanBeWiredInADependency() {
        // Arrange
        WireRepository wireRepository = WireRepository.open();

        // Act
        // Assert
        assertThat(wireRepository.contains(Algorithms.class)).isTrue();
        assertThat(wireRepository.get(AlgorithmsDependency.class).algorithms()).isNotNull();
    }
}