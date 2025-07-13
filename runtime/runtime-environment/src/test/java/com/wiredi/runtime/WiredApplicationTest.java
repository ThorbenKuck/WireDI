package com.wiredi.runtime;

import com.wiredi.runtime.domain.Disposable;
import com.wiredi.runtime.domain.Eager;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.domain.scopes.exceptions.ScopeNotActivatedException;
import com.wiredi.runtime.exceptions.BeanNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class WiredApplicationTest {

    @Nested
    class Lifecycle {

        @Test
        void startupAndShutdown() {
            // Arrange
            Case testCase = new Case();
            WiredApplicationInstance application = WiredApplication.start(container -> {
                container.announce(
                        IdentifiableProvider.singleton(testCase)
                                .withAdditionalTypeIdentifier(TypeIdentifier.just(Eager.class))
                                .withAdditionalTypeIdentifier(TypeIdentifier.just(Disposable.class))
                );
            });
            WireContainer wireContainer = application.wireContainer();

            // Act
            assertThat(wireContainer.tryGet(Case.class)).isPresent().contains(testCase);
            assertThat(wireContainer.getAll(Eager.class)).containsExactly(testCase);
            assertThat(wireContainer.getAll(Disposable.class)).contains(testCase);
            application.shutdown();
            assertThatCode(() -> wireContainer.get(Case.class)).isInstanceOf(ScopeNotActivatedException.class).hasMessage("Tried to access inactive scope SingletonScope{active=false}");
            assertThat(wireContainer.tryGet(Case.class)).isEmpty();
            assertThat(wireContainer.getAll(Eager.class)).isEmpty();
            assertThat(wireContainer.getAll(Disposable.class)).isEmpty();

            // Assert
            assertThat(testCase.wasInitialized).withFailMessage(() -> "TestCase was not initialized").isTrue();
            assertThat(testCase.wasTornDown).withFailMessage(() -> "TestCase was not torn down").isTrue();
        }

        static class Case implements Eager, Disposable {

            boolean wasInitialized = false;
            boolean wasTornDown = false;

            @Override
            public void tearDown(WireContainer origin) {
                wasTornDown = true;
            }

            @Override
            public void setup(WireContainer wireContainer) {
                wasInitialized = true;
            }
        }
    }


}