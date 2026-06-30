package com.wiredi.compiler.tests.elements;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;
import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Mock implementation of PrimitiveType for testing purposes.
 */
public class MockPrimitiveType implements PrimitiveType {
    private final Class<?> primitiveClass;

    public MockPrimitiveType(Class<?> primitiveClass) {
        this.primitiveClass = primitiveClass;
    }

    @Override
    public TypeKind getKind() {
        if (primitiveClass == boolean.class) return TypeKind.BOOLEAN;
        if (primitiveClass == byte.class) return TypeKind.BYTE;
        if (primitiveClass == short.class) return TypeKind.SHORT;
        if (primitiveClass == int.class) return TypeKind.INT;
        if (primitiveClass == long.class) return TypeKind.LONG;
        if (primitiveClass == char.class) return TypeKind.CHAR;
        if (primitiveClass == float.class) return TypeKind.FLOAT;
        if (primitiveClass == double.class) return TypeKind.DOUBLE;
        return TypeKind.NONE;
    }

    @Override
    public <R, P> R accept(TypeVisitor<R, P> v, P p) {
        return v.visitPrimitive(this, p);
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
        return List.of();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return null;
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        return null;
    }

    @Override
    public String toString() {
        return primitiveClass.getName();
    }
}