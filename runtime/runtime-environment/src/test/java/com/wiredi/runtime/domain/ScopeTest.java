package com.wiredi.runtime.domain;

import com.wiredi.runtime.domain.factories.MissingBeanException;
import com.wiredi.runtime.domain.provider.QualifiedTypeIdentifier;
import com.wiredi.runtime.domain.provider.SimpleProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class ScopeTest {

    @Test
    public void settingTheFirstValueAsNullAllowsForOverridesLater() {
        // Arrange
        Scope scope = Scope.singleton();
        scope.link(Mockito.mock());
        scope.start();

        // Act
        assertThrows(MissingBeanException.class, () -> scope.get(TypeIdentifier.just(A.class)));
        scope.register(SimpleProvider.builder(TypeIdentifier.just(A.class)).withInstance(A::new).build());
        A shouldBeNonNull = assertDoesNotThrow(() -> scope.get(TypeIdentifier.just(A.class)));

        // Assert
        assertThat(shouldBeNonNull).isNotNull();
    }

    @Test
    public void testThatSingletonScopeWorks() {
        // Arrange
        Scope scope = Scope.singleton();
        scope.link(Mockito.mock());
        scope.register(SimpleProvider.builder(TypeIdentifier.just(A.class)).withInstance(A::new).build());
        scope.start();

        // Act
        A a1 = scope.get(TypeIdentifier.just(A.class));
        A a2 = scope.get(TypeIdentifier.just(A.class));
        assertThrows(MissingBeanException.class, () -> scope.get(TypeIdentifier.just(B.class)));

        // Assert
        assertSame(a1, a2);
    }

    @Test
    public void testThatPrototypeScopeWorks() {
        // Arrange
        Scope scope = Scope.prototype();
        scope.link(Mockito.mock());
        scope.register(SimpleProvider.builder(TypeIdentifier.just(A.class)).withInstance(A::new).build());
        scope.start();

        // Act
        A a1 = scope.get(TypeIdentifier.just(A.class));
        A a2 = scope.get(TypeIdentifier.just(A.class));
        assertThrows(MissingBeanException.class, () -> scope.get(TypeIdentifier.just(B.class)));

        // Assert
        assertNotSame(a1, a2);
    }

    @Test
    public void testThatCompositeScopeWorks() {
        // Arrange
        Scope singletonScope = Scope.singleton();
        Scope prototypeScope = Scope.prototype();
        Scope compositeScope = Scope.composite(singletonScope, prototypeScope);
        compositeScope.link(Mockito.mock());
        prototypeScope.register(SimpleProvider.builder(TypeIdentifier.just(A.class)).withInstance(A::new).build());
        singletonScope.register(SimpleProvider.builder(TypeIdentifier.just(B.class)).withInstance(() -> new B(new A())).build());
        compositeScope.start();

        // Act
        A a1 = compositeScope.get(TypeIdentifier.just(A.class));
        A a2 = compositeScope.get(TypeIdentifier.just(A.class));
        B b1 = compositeScope.get(TypeIdentifier.just(B.class));
        B b2 = compositeScope.get(TypeIdentifier.just(B.class));

        // Assert
        assertNotSame(a1, a2);
        assertSame(b1, b2);
    }

    static class A {
    }

    record B(A a) {
    }
}


