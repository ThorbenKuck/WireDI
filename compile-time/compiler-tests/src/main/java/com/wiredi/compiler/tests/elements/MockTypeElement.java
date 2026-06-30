package com.wiredi.compiler.tests.elements;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Mock implementation of TypeElement for testing purposes.
 * This class bridges the gap between runtime Class objects and compile-time TypeElement representations.
 */
public class MockTypeElement implements TypeElement {
    private final Class<?> clazz;
    private final MockTypeMirror typeMirror;
    private List<AnnotationMirror> annotationMirrors;

    public MockTypeElement(Class<?> clazz) {
        this.clazz = clazz;
        this.typeMirror = new MockTypeMirror(clazz);
    }

    /**
     * Factory method to create a MockTypeElement from a class.
     */
    public static MockTypeElement from(Class<?> clazz) {
        return new MockTypeElement(clazz);
    }

    @Override
    public TypeMirror asType() {
        return typeMirror;
    }

    @Override
    public ElementKind getKind() {
        if (clazz.isInterface()) {
            return ElementKind.INTERFACE;
        } else if (clazz.isEnum()) {
            return ElementKind.ENUM;
        } else if (clazz.isAnnotation()) {
            return ElementKind.ANNOTATION_TYPE;
        } else {
            return ElementKind.CLASS;
        }
    }

    @Override
    public Set<Modifier> getModifiers() {
        Set<Modifier> modifiers = new HashSet<>();
        int mod = clazz.getModifiers();

        if (java.lang.reflect.Modifier.isPublic(mod)) modifiers.add(Modifier.PUBLIC);
        if (java.lang.reflect.Modifier.isPrivate(mod)) modifiers.add(Modifier.PRIVATE);
        if (java.lang.reflect.Modifier.isProtected(mod)) modifiers.add(Modifier.PROTECTED);
        if (java.lang.reflect.Modifier.isStatic(mod)) modifiers.add(Modifier.STATIC);
        if (java.lang.reflect.Modifier.isFinal(mod)) modifiers.add(Modifier.FINAL);
        if (java.lang.reflect.Modifier.isAbstract(mod)) modifiers.add(Modifier.ABSTRACT);

        return modifiers;
    }

    @Override
    public Name getSimpleName() {
        return new MockName(clazz.getSimpleName());
    }

    @Override
    public Name getQualifiedName() {
        return new MockName(clazz.getName());
    }

    @Override
    public TypeMirror getSuperclass() {
        Class<?> superclass = clazz.getSuperclass();
        if (superclass == null || clazz == Object.class) {
            return new MockNoType(TypeKind.NONE);
        }
        return new MockTypeMirror(superclass);
    }

    @Override
    public List<? extends TypeMirror> getInterfaces() {
        return Arrays.stream(clazz.getInterfaces())
                .map(MockTypeMirror::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<? extends TypeParameterElement> getTypeParameters() {
        // Simplified: not implementing generic type parameters for now
        return List.of();
    }

    @Override
    public Element getEnclosingElement() {
        Class<?> enclosingClass = clazz.getEnclosingClass();
        if (enclosingClass != null) {
            return new MockTypeElement(enclosingClass);
        }
        // Return package element
        return new MockPackageElement(clazz.getPackage());
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
        List<Element> elements = new ArrayList<>();
        
        // Add fields
        Arrays.stream(clazz.getDeclaredFields())
                .map(MockVariableElement::new)
                .forEach(elements::add);
        
        // Add methods
        Arrays.stream(clazz.getDeclaredMethods())
                .map(MockExecutableElement::new)
                .forEach(elements::add);
        
        // Add constructors
        Arrays.stream(clazz.getDeclaredConstructors())
                .map(MockExecutableElement::new)
                .forEach(elements::add);
        
        // Add nested classes
        Arrays.stream(clazz.getDeclaredClasses())
                .map(MockTypeElement::new)
                .forEach(elements::add);
        
        return elements;
    }

    @Override
    public NestingKind getNestingKind() {
        if (clazz.isAnonymousClass()) {
            return NestingKind.ANONYMOUS;
        } else if (clazz.isLocalClass()) {
            return NestingKind.LOCAL;
        } else if (clazz.isMemberClass()) {
            return NestingKind.MEMBER;
        } else {
            return NestingKind.TOP_LEVEL;
        }
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
        if (annotationMirrors == null) {
            annotationMirrors = Arrays.stream(clazz.getAnnotations())
                    .map(MockAnnotationMirror::new)
                    .collect(Collectors.toList());
        }
        return annotationMirrors;
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return clazz.getAnnotation(annotationType);
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        return clazz.getAnnotationsByType(annotationType);
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p) {
        return v.visitType(this, p);
    }

    public Class<?> getUnderlyingClass() {
        return clazz;
    }

    @Override
    public String toString() {
        return clazz.getName();
    }
}