package com.wiredi.runtime;

import com.wiredi.runtime.domain.Disposable;
import com.wiredi.runtime.domain.Eager;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WiredApplicationTest {

    @Nested
    class Lifecycle {

        @Test
        void startupAndShutdown() {
            // Arrange
            Case testCase = new Case();
            WiredApplicationInstance application = WiredApplication.start(repository -> {
                repository.announce(
                        IdentifiableProvider.singleton(testCase)
                                .withAdditionalTypeIdentifier(TypeIdentifier.just(Eager.class))
                                .withAdditionalTypeIdentifier(TypeIdentifier.just(Disposable.class))
                );
            });
            WireRepository wireRepository = application.wireRepository();

            // Act
            assertThat(wireRepository.tryGet(Case.class)).isPresent().contains(testCase);
            assertThat(wireRepository.getAll(Eager.class)).containsExactly(testCase);
            assertThat(wireRepository.getAll(Disposable.class)).contains(testCase);
            application.shutdown();
            assertThat(wireRepository.tryGet(Case.class)).isEmpty();
            assertThat(wireRepository.getAll(Eager.class)).isEmpty();
            assertThat(wireRepository.getAll(Disposable.class)).isEmpty();

            // Assert
            assertThat(testCase.wasInitialized).withFailMessage(() -> "TestCase was not initialized").isTrue();
            assertThat(testCase.wasTornDown).withFailMessage(() -> "TestCase was not torn down").isTrue();
        }

        static class Case implements Eager, Disposable {

            boolean wasInitialized = false;
            boolean wasTornDown = false;

            @Override
            public void tearDown(WireRepository origin) {
                wasTornDown = true;
            }

            @Override
            public void setup(WireRepository wireRepository) {
                wasInitialized = true;
            }
        }
    }


}