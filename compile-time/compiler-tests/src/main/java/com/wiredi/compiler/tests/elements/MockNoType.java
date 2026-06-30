package com.wiredi.compiler.tests.elements;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;
import java.lang.annotation.Annotation;
import java.util.List;


public class MockNoType implements NoType {

    private final TypeKind kind;

    public MockNoType() {
        this(TypeKind.NONE);
    }

    public MockNoType(TypeKind kind) {
        this.kind = kind;
    }

    @Override
    public TypeKind getKind() {
        return kind;
    }

    @Override
    public <R, P> R accept(TypeVisitor<R, P> v, P p) {
        return v.visitNoType(this, p);
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
        return kind.toString().toLowerCase();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NoType other)) return false;
        return kind == other.getKind();
    }

    @Override
    public int hashCode() {
        return kind.hashCode();
    }
}