package com.wiredi.runtime.domain.conditional.builtin;

import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.AnnotationMetaData;
import com.wiredi.runtime.domain.provider.SimpleProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.domain.provider.condition.LoadCondition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionalOnMissingClassTest {

    @Test
    void shouldLoadComponentWhenClassDoesNotExist() {
        // Arrange
        WireRepository repository = WireRepository.create();

        // Create a provider with ConditionalOnMissingClass for a non-existent class
        SimpleProvider<TestComponent> provider = SimpleProvider.builder(new TestComponent())
                .withCondition(
                        LoadCondition.of(ConditionalOnMissingClassEvaluator.class, AnnotationMetaData.newInstance(ConditionalOnMissingClass.class)
                                .withField("className", "com.nonexistent.SomeClass"))
                ).build();

        // Act
        repository.announce(provider);
        repository.load();

        // Assert
        assertTrue(repository.contains(TypeIdentifier.of(TestComponent.class)));
    }

    @Test
    void shouldNotLoadComponentWhenClassExists() {
        // Arrange
        WireRepository repository = WireRepository.create();

        // Create a provider with ConditionalOnMissingClass for String (which always exists)
        SimpleProvider<TestComponent> provider = SimpleProvider.builder(new TestComponent())
                .withCondition(
                        LoadCondition.of(ConditionalOnMissingClassEvaluator.class, AnnotationMetaData.newInstance(ConditionalOnMissingClass.class)
                                .withField("className", "java.lang.String"))
                ).build();

        // Act
        repository.announce(provider);
        repository.load();

        // Assert
        assertFalse(repository.contains(TypeIdentifier.of(TestComponent.class)));
    }

    // Test components
    static class TestComponent {
    }
}
