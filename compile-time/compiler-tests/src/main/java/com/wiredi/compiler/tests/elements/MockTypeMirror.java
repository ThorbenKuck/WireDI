package com.wiredi.compiler.tests.elements;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.*;
import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Mock implementation of TypeMirror for testing purposes.
 */
public class MockTypeMirror implements DeclaredType {
    private final Class<?> clazz;

    public MockTypeMirror(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public TypeKind getKind() {
        if (clazz == void.class) {
            return TypeKind.VOID;
        } else if (clazz == boolean.class) {
            return TypeKind.BOOLEAN;
        } else if (clazz == byte.class) {
            return TypeKind.BYTE;
        } else if (clazz == short.class) {
            return TypeKind.SHORT;
        } else if (clazz == int.class) {
            return TypeKind.INT;
        } else if (clazz == long.class) {
            return TypeKind.LONG;
        } else if (clazz == char.class) {
            return TypeKind.CHAR;
        } else if (clazz == float.class) {
            return TypeKind.FLOAT;
        } else if (clazz == double.class) {
            return TypeKind.DOUBLE;
        } else if (clazz.isArray()) {
            return TypeKind.ARRAY;
        } else {
            return TypeKind.DECLARED;
        }
    }

    @Override
    public <R, P> R accept(TypeVisitor<R, P> v, P p) {
        TypeKind kind = getKind();
        switch (kind) {
            case DECLARED:
                return v.visitDeclared(this, p);
            case ARRAY:
                return v.visitArray(new MockArrayType(clazz), p);
            case VOID:
            case BOOLEAN:
            case BYTE:
            case SHORT:
            case INT:
            case LONG:
            case CHAR:
            case FLOAT:
            case DOUBLE:
                return v.visitPrimitive(new MockPrimitiveType(clazz), p);
            default:
                return v.visitUnknown(this, p);
        }
    }

    @Override
    public javax.lang.model.element.Element asElement() {
        if (clazz.isPrimitive()) {
            throw new IllegalStateException("Primitive types don't have an element");
        }
        return new MockTypeElement(clazz);
    }

    @Override
    public TypeMirror getEnclosingType() {
        Class<?> enclosingClass = clazz.getEnclosingClass();
        if (enclosingClass != null) {
            return new MockTypeMirror(enclosingClass);
        }
        return new MockNoType(TypeKind.NONE);
    }

    @Override
    public List<? extends TypeMirror> getTypeArguments() {
        // Simplified: not implementing generic type arguments for now
        return List.of();
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
        if (clazz.isPrimitive()) {
            return List.of();
        }
        return new MockTypeElement(clazz).getAnnotationMirrors();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return clazz.getAnnotation(annotationType);
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        return clazz.getAnnotationsByType(annotationType);
    }

    public Class<?> getUnderlyingClass() {
        return clazz;
    }

    @Override
    public String toString() {
        return clazz.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MockTypeMirror other)) return false;
        return clazz.equals(other.clazz);
    }

    @Override
    public int hashCode() {
        return clazz.hashCode();
    }
}
