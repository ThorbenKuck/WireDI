package com.wiredi.runtime.domain.provider;

import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.AnnotationMetaData;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnClass;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnClassEvaluator;
import com.wiredi.runtime.domain.provider.condition.LoadCondition;
import com.wiredi.runtime.qualifier.QualifierType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SimpleProviderTest {

    @Test
    void shouldCreateInstanceWithDefaultConstructor() {
        // Arrange
        SimpleProvider<TestComponent> provider = SimpleProvider.builder(new TestComponent()).build();
        WireRepository repository = mock(WireRepository.class);

        // Act
        TestComponent instance = provider.get(repository);

        // Assert
        assertNotNull(instance);
        assertEquals(TestComponent.class, instance.getClass());
    }

    @Test
    void shouldUseCustomInstanceSupplier() {
        // Arrange
        TestComponent expectedInstance = new TestComponent();
        SimpleProvider<TestComponent> provider = SimpleProvider.builder(TestComponent.class)
                .withInstance(() -> expectedInstance)
                .build();
        WireRepository repository = mock(WireRepository.class);

        // Act
        TestComponent instance = provider.get(repository);

        // Assert
        assertSame(expectedInstance, instance);
    }

    @Test
    void shouldUseFixedInstance() {
        // Arrange
        TestComponent expectedInstance = new TestComponent();
        SimpleProvider<TestComponent> provider = SimpleProvider.builder(TestComponent.class)
                .withInstance(expectedInstance)
                .build();
        WireRepository repository = mock(WireRepository.class);

        // Act
        TestComponent instance1 = provider.get(repository);
        TestComponent instance2 = provider.get(repository);

        // Assert
        assertSame(expectedInstance, instance1);
        assertSame(expectedInstance, instance2);
        assertSame(instance1, instance2);
    }

    @Test
    void shouldSupportAdditionalWireTypes() {
        // Arrange
        TypeIdentifier<TestInterface> interfaceType = TypeIdentifier.of(TestInterface.class);
        SimpleProvider<TestComponent> provider = SimpleProvider.builder(TestComponent.class)
                .withAdditionalType(interfaceType)
                .withInstance(new TestComponent())
                .build();

        // Act & Assert
        List<TypeIdentifier<?>> additionalTypes = provider.additionalWireTypes();
        assertEquals(1, additionalTypes.size());
        assertTrue(additionalTypes.contains(interfaceType));
    }

    @Test
    void shouldSupportQualifiers() {
        // Arrange
        QualifierType qualifier = QualifierType.just("primary");
        SimpleProvider<TestComponent> provider = SimpleProvider.builder(new TestComponent())
                .withQualifier(qualifier)
                .build();

        // Act & Assert
        List<QualifierType> qualifiers = provider.qualifiers();
        assertEquals(1, qualifiers.size());
        assertTrue(qualifiers.contains(qualifier));
    }

    @Test
    void shouldSupportStringQualifiers() {
        // Arrange
        SimpleProvider<TestComponent> provider = SimpleProvider.builder(TestComponent.class)
                .withQualifier("primary")
                .withInstance(new TestComponent())
                .build();

        // Act & Assert
        List<QualifierType> qualifiers = provider.qualifiers();
        assertEquals(1, qualifiers.size());
        assertEquals("primary", qualifiers.getFirst().name());
    }

    @Test
    void shouldSupportCustomOrder() {
        // Arrange
        int expectedOrder = 42;
        SimpleProvider<TestComponent> provider = SimpleProvider.builder(TestComponent.class)
                .withOrder(expectedOrder)
                .withInstance(new TestComponent())
                .build();

        // Act & Assert
        assertEquals(expectedOrder, provider.getOrder());
    }

    @Test
    void shouldSupportNonSingletonProviders() {
        // Arrange
        SimpleProvider<TestComponent> provider = SimpleProvider.builder(TestComponent.class)
                .withInstance(new TestComponent())
                .withSingleton(false)
                .build();

        // Act & Assert
        assertFalse(provider.isSingleton());
    }

    @Test
    void shouldSupportSingletonProviders() {
        // Arrange
        WireRepository repository = WireRepository.create();
        SimpleProvider<TestComponent> provider = SimpleProvider.builder(TestComponent.class)
                .withInstance(TestComponent::new)
                .withSingleton(true)
                .build();

        // Act & Assert
        assertTrue(provider.isSingleton());
        assertSame(provider.get(repository), provider.get(repository));
    }

    @Test
    void shouldSupportDirectLoadCondition() {
        // Arrange
        LoadCondition condition = LoadCondition.TRUE;
        SimpleProvider<TestComponent> provider = SimpleProvider.builder(TestComponent.class)
                .withCondition(condition)
                .withInstance(new TestComponent())
                .build();

        // Act & Assert
        assertSame(condition, provider.condition());
    }

    @Test
    void shouldCreateConditionFromEvaluatorAndFields() {
        // Arrange
        SimpleProvider<TestComponent> provider = SimpleProvider.builder(TestComponent.class)
                .withCondition(
                        ConditionalOnClassEvaluator.class, builder -> builder.withAnnotation(
                                AnnotationMetaData.builder(ConditionalOnClass.class)
                                        .withField("className", "java.lang.String")
                                        .build()
                        ).build()
                )
                .withInstance(new TestComponent())
                .build();

        // Act & Assert
        assertNotNull(provider.condition());
    }

    // Test classes
    interface TestInterface {
    }

    static class TestComponent implements TestInterface {
    }
}
