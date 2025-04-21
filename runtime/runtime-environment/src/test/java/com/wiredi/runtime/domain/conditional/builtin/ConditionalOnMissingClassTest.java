package com.wiredi.runtime.domain.conditional.builtin;

import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.AnnotationMetaData;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.domain.provider.condition.SingleLoadCondition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionalOnMissingClassTest {

    @Test
    void shouldLoadComponentWhenClassDoesNotExist() {
        // Arrange
        WireRepository repository = WireRepository.create();

        // Create a provider with ConditionalOnMissingClass for a non-existent class
        repository.announce(
                IdentifiableProvider.singleton(new ConditionalOnClassTest.TestComponentWithExistingClass())
                        .withLoadCondition(new SingleLoadCondition(ConditionalOnClassEvaluator.class, AnnotationMetaData.newInstance(ConditionalOnClass.class)
                                .withField("className", "com.nonexistent.SomeClass")
                                .build()))
        );

        // Act
        repository.load();

        // Assert
        assertTrue(repository.contains(TypeIdentifier.of(TestComponentWithMissingClass.class)));
    }

    @Test
    void shouldNotLoadComponentWhenClassExists() {
        // Arrange
        WireRepository repository = WireRepository.create();

        // Create a provider with ConditionalOnMissingClass for String (which always exists)
        repository.announce(
                IdentifiableProvider.singleton(new ConditionalOnClassTest.TestComponentWithExistingClass())
                        .withLoadCondition(new SingleLoadCondition(ConditionalOnClassEvaluator.class, AnnotationMetaData.newInstance(ConditionalOnClass.class)
                                .withField("className", "java.lang.String")
                                .build()))
        );

        // Act
        repository.load();

        // Assert
        assertFalse(repository.contains(TypeIdentifier.of(TestComponentWithExistingClass.class)));
    }

    // Test components
    static class TestComponentWithMissingClass {
    }

    static class TestComponentWithExistingClass {
    }
}
