package com.wiredi.compiler.tests.elements;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Mock implementation of ArrayType for testing purposes.
 */
public class MockArrayType implements ArrayType {
    private final Class<?> arrayClass;
    private final TypeMirror componentType;

    public MockArrayType(Class<?> arrayClass) {
        if (!arrayClass.isArray()) {
            throw new IllegalArgumentException("Class must be an array type");
        }
        this.arrayClass = arrayClass;
        this.componentType = new MockTypeMirror(arrayClass.getComponentType());
    }

    @Override
    public TypeMirror getComponentType() {
        return componentType;
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.ARRAY;
    }

    @Override
    public <R, P> R accept(TypeVisitor<R, P> v, P p) {
        return v.visitArray(this, p);
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
        return List.of();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return arrayClass.getAnnotation(annotationType);
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        return arrayClass.getAnnotationsByType(annotationType);
    }

    @Override
    public String toString() {
        return componentType.toString() + "[]";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ArrayType other)) return false;
        return componentType.equals(other.getComponentType());
    }
    
    @Override
    public int hashCode() {
        return componentType.hashCode();
    }
}