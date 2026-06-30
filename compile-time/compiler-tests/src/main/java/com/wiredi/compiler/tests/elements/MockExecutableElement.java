package com.wiredi.compiler.tests.elements;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Mock implementation of ExecutableElement for testing purposes.
 */
public class MockExecutableElement implements ExecutableElement {
    private final Executable executable;

    public MockExecutableElement(Executable method) {
        this.executable = method;
    }

    @Override
    public List<? extends TypeParameterElement> getTypeParameters() {
        return List.of();
    }

    @Override
    public TypeMirror getReturnType() {
        if (executable instanceof Method) {
            return new MockTypeMirror(((Method) executable).getReturnType());
        }
        return new MockTypeMirror(void.class);
    }

    @Override
    public List<? extends VariableElement> getParameters() {
        return Arrays.stream(executable.getParameters())
                .map(MockVariableElement::new)
                .collect(Collectors.toList());
    }

    @Override
    public TypeMirror getReceiverType() {
        return new MockNoType();
    }

    @Override
    public boolean isVarArgs() {
        return executable.isVarArgs();
    }

    @Override
    public boolean isDefault() {
        if (executable instanceof Method) {
            return ((Method) executable).isDefault();
        }
        return false;
    }

    @Override
    public List<? extends TypeMirror> getThrownTypes() {
        return Arrays.stream(executable.getExceptionTypes())
                .map(MockTypeMirror::new)
                .collect(Collectors.toList());
    }

    @Override
    public AnnotationValue getDefaultValue() {
        if (executable instanceof Method) {
            Object defaultValue = ((Method) executable).getDefaultValue();
            if (defaultValue != null) {
                return new MockAnnotationValue(defaultValue);
            }
        }
        return null;
    }

    @Override
    public Name getSimpleName() {
        if (executable instanceof Constructor) {
            return new MockName("<init>");
        }
        return new MockName(executable.getName());
    }

    @Override
    public TypeMirror asType() {
        // Return a method type (simplified)
        return new MockTypeMirror(executable.getDeclaringClass());
    }

    @Override
    public ElementKind getKind() {
        if (executable instanceof Constructor) {
            return ElementKind.CONSTRUCTOR;
        }
        return ElementKind.METHOD;
    }

    @Override
    public Set<Modifier> getModifiers() {
        Set<Modifier> modifiers = new HashSet<>();
        int mod = executable.getModifiers();

        if (java.lang.reflect.Modifier.isPublic(mod)) modifiers.add(Modifier.PUBLIC);
        if (java.lang.reflect.Modifier.isPrivate(mod)) modifiers.add(Modifier.PRIVATE);
        if (java.lang.reflect.Modifier.isProtected(mod)) modifiers.add(Modifier.PROTECTED);
        if (java.lang.reflect.Modifier.isStatic(mod)) modifiers.add(Modifier.STATIC);
        if (java.lang.reflect.Modifier.isFinal(mod)) modifiers.add(Modifier.FINAL);
        if (java.lang.reflect.Modifier.isAbstract(mod)) modifiers.add(Modifier.ABSTRACT);
        if (java.lang.reflect.Modifier.isSynchronized(mod)) modifiers.add(Modifier.SYNCHRONIZED);

        return modifiers;
    }

    @Override
    public Element getEnclosingElement() {
        return new MockTypeElement(executable.getDeclaringClass());
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
        return List.of();
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
        return Arrays.stream(executable.getAnnotations())
                .map(MockAnnotationMirror::new)
                .collect(Collectors.toList());
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return executable.getAnnotation(annotationType);
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        return executable.getAnnotationsByType(annotationType);
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p) {
        return v.visitExecutable(this, p);
    }

    @Override
    public String toString() {
        return executable.toString();
    }
}