package com.wiredi.runtime;

import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.exceptions.BeanNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ObjectReferenceTest {

    @Mock
    private WireContainer mockWireContainer;

    private TypeIdentifier<TestService> typeIdentifier;
    private ObjectReference<TestService> objectReference;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        typeIdentifier = TypeIdentifier.of(TestService.class);
        objectReference = new ObjectReference<>(mockWireContainer, typeIdentifier);
    }

    @Test
    void wireContainer_shouldReturnTheWireContainer() {
        // Act
        WireContainer result = objectReference.wireContainer();

        // Assert
        assertThat(result).isSameAs(mockWireContainer);
    }

    @Test
    void getAll_shouldReturnAllInstancesFromWireContainer() {
        // Arrange
        List<TestService> expectedServices = List.of(new TestService(), new TestService());
        when(mockWireContainer.getAll(typeIdentifier)).thenReturn(expectedServices);

        // Act
        Collection<TestService> result = objectReference.getAll();

        // Assert
        assertThat(result).isEqualTo(expectedServices);
        verify(mockWireContainer).getAll(typeIdentifier);
    }

    @Test
    void getInstance_shouldReturnInstanceFromWireContainer() {
        // Arrange
        TestService expectedService = new TestService();
        when(mockWireContainer.get(typeIdentifier)).thenReturn(expectedService);

        // Act
        TestService result = objectReference.getInstance();

        // Assert
        assertThat(result).isSameAs(expectedService);
        verify(mockWireContainer).get(typeIdentifier);
    }

    @Test
    void getInstance_shouldReturnNullWhenExceptionIsThrown() {
        // Arrange
        when(mockWireContainer.get(typeIdentifier)).thenThrow(new BeanNotFoundException(typeIdentifier, mockWireContainer));

        // Act
        TestService result = objectReference.getInstance();

        // Assert
        assertThat(result).isNull();
        verify(mockWireContainer).get(typeIdentifier);
    }

    @Test
    void getInstance_withSupplier_shouldReturnInstanceFromWireContainer() {
        // Arrange
        TestService expectedService = new TestService();
        when(mockWireContainer.get(typeIdentifier)).thenReturn(expectedService);
        Supplier<TestService> defaultSupplier = mock(Supplier.class);

        // Act
        TestService result = objectReference.getInstance(defaultSupplier);

        // Assert
        assertThat(result).isSameAs(expectedService);
        verify(mockWireContainer).get(typeIdentifier);
        verifyNoInteractions(defaultSupplier);
    }

    @Test
    void getInstance_withSupplier_shouldUseSupplierWhenInstanceIsNull() {
        // Arrange
        when(mockWireContainer.get(typeIdentifier)).thenReturn(null);
        TestService defaultService = new TestService();
        Supplier<TestService> defaultSupplier = () -> defaultService;

        // Act
        TestService result = objectReference.getInstance(defaultSupplier);

        // Assert
        assertThat(result).isSameAs(defaultService);
        verify(mockWireContainer).get(typeIdentifier);
    }

    @Test
    void ifAvailable_shouldExecuteConsumerWhenInstanceIsAvailable() {
        // Arrange
        TestService service = new TestService();
        when(mockWireContainer.get(typeIdentifier)).thenReturn(service);
        AtomicBoolean consumerCalled = new AtomicBoolean(false);
        AtomicReference<TestService> passedService = new AtomicReference<>();

        Consumer<TestService> consumer = s -> {
            consumerCalled.set(true);
            passedService.set(s);
        };

        // Act
        objectReference.ifAvailable(consumer);

        // Assert
        assertThat(consumerCalled.get()).isTrue();
        assertThat(passedService.get()).isSameAs(service);
        verify(mockWireContainer).get(typeIdentifier);
    }

    @Test
    void ifAvailable_shouldNotExecuteConsumerWhenInstanceIsNotAvailable() {
        // Arrange
        when(mockWireContainer.get(typeIdentifier)).thenReturn(null);
        Consumer<TestService> consumer = mock(Consumer.class);

        // Act
        objectReference.ifAvailable(consumer);

        // Assert
        verifyNoInteractions(consumer);
        verify(mockWireContainer).get(typeIdentifier);
    }

    @Test
    void ifAvailable_withClass_shouldExecuteConsumerWhenInstanceIsAvailableAndAssignable() {
        // Arrange
        TestServiceImpl service = new TestServiceImpl();
        when(mockWireContainer.get(typeIdentifier)).thenReturn(service);
        AtomicBoolean consumerCalled = new AtomicBoolean(false);
        AtomicReference<TestServiceImpl> passedService = new AtomicReference<>();

        Consumer<TestServiceImpl> consumer = s -> {
            consumerCalled.set(true);
            passedService.set(s);
        };

        // Act
        objectReference.ifAvailable(TestServiceImpl.class, consumer);

        // Assert
        assertThat(consumerCalled.get()).isTrue();
        assertThat(passedService.get()).isSameAs(service);
        verify(mockWireContainer).get(typeIdentifier);
    }

    @Test
    void ifAvailable_withClass_shouldNotExecuteConsumerWhenInstanceIsNotAssignable() {
        // Arrange
        TestService service = new TestService(); // Not a TestServiceImpl
        when(mockWireContainer.get(typeIdentifier)).thenReturn(service);
        Consumer<TestServiceImpl> consumer = mock(Consumer.class);

        // Act
        objectReference.ifAvailable(TestServiceImpl.class, consumer);

        // Assert
        verifyNoInteractions(consumer);
        verify(mockWireContainer).get(typeIdentifier);
    }

    // Helper classes for testing
    static class TestService {
    }

    static class TestServiceImpl extends TestService {
    }
}
