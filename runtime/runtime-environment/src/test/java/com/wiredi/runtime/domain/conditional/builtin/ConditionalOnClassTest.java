package com.wiredi.runtime.domain.conditional.builtin;

import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.AnnotationMetaData;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.domain.provider.condition.SingleLoadCondition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionalOnClassTest {

    @Test
    void shouldLoadComponentWhenClassExists() {
        // Arrange
        WireRepository repository = WireRepository.create();

        // Create a provider with ConditionalOnClass for String (which always exists)
        repository.announce(
                IdentifiableProvider.singleton(new TestComponentWithExistingClass())
                        .withLoadCondition(new SingleLoadCondition(ConditionalOnClassEvaluator.class, AnnotationMetaData.newInstance(ConditionalOnClass.class)
                                .withField("className", "java.lang.String")
                                .build()))
        );

        // Act
        repository.load();

        // Assert
        assertTrue(repository.contains(TypeIdentifier.of(TestComponentWithExistingClass.class)));
    }

    @Test
    void shouldNotLoadComponentWhenClassDoesNotExist() {
        // Arrange
        WireRepository repository = WireRepository.create();
        repository.announce(
                IdentifiableProvider.singleton(new TestComponentWithExistingClass())
                        .withLoadCondition(new SingleLoadCondition(ConditionalOnClassEvaluator.class, AnnotationMetaData.newInstance(ConditionalOnClass.class)
                                .withField("className", "com.nonexistent.SomeClass")
                                .build()))
        );

        // Act
        repository.load();

        // Assert
        assertFalse(repository.contains(TypeIdentifier.of(TestComponentWithNonExistingClass.class)));
    }

    // Test components
    static class TestComponentWithExistingClass {
    }

    static class TestComponentWithNonExistingClass {
    }
}
