package com.wiredi.compiler.tests.elements;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

/**
 * Mock implementation of PackageElement for testing purposes.
 */
public class MockPackageElement implements PackageElement {
    private final Package pkg;

    public MockPackageElement(Package pkg) {
        this.pkg = pkg;
    }

    @Override
    public Name getQualifiedName() {
        return new MockName(pkg != null ? pkg.getName() : "");
    }

    @Override
    public Name getSimpleName() {
        if (pkg == null) return new MockName("");
        String name = pkg.getName();
        int lastDot = name.lastIndexOf('.');
        return new MockName(lastDot >= 0 ? name.substring(lastDot + 1) : name);
    }

    @Override
    public boolean isUnnamed() {
        return pkg == null || pkg.getName().isEmpty();
    }

    @Override
    public TypeMirror asType() {
        return new MockNoType();
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.PACKAGE;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return Set.of();
    }

    @Override
    public Element getEnclosingElement() {
        return null;
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
        return List.of();
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
        return List.of();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return pkg != null ? pkg.getAnnotation(annotationType) : null;
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        if (pkg != null) {
            return pkg.getAnnotationsByType(annotationType);
        }
        @SuppressWarnings("unchecked")
        A[] empty = (A[]) java.lang.reflect.Array.newInstance(annotationType, 0);
        return empty;
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p) {
        return v.visitPackage(this, p);
    }

    @Override
    public String toString() {
        return pkg != null ? pkg.getName() : "";
    }
}