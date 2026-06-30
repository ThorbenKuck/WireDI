package com.wiredi.compiler.tests.elements;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Mock implementation of VariableElement for testing purposes.
 */
public class MockVariableElement implements VariableElement {
    private final Object source; // Can be Field, Parameter, or Enum constant

    public MockVariableElement(Field field) {
        this.source = field;
    }

    public MockVariableElement(Parameter parameter) {
        this.source = parameter;
    }

    public MockVariableElement(Enum<?> enumConstant) {
        this.source = enumConstant;
    }

    @Override
    public Object getConstantValue() {
        if (source instanceof Field field) {
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                try {
                    field.setAccessible(true);
                    return field.get(null);
                } catch (IllegalAccessException e) {
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    public Name getSimpleName() {
        if (source instanceof Field) {
            return new MockName(((Field) source).getName());
        } else if (source instanceof Parameter) {
            return new MockName(((Parameter) source).getName());
        } else if (source instanceof Enum) {
            return new MockName(((Enum<?>) source).name());
        }
        return new MockName("unknown");
    }

    @Override
    public TypeMirror asType() {
        if (source instanceof Field) {
            return new MockTypeMirror(((Field) source).getType());
        } else if (source instanceof Parameter) {
            return new MockTypeMirror(((Parameter) source).getType());
        } else if (source instanceof Enum) {
            return new MockTypeMirror(((Enum<?>) source).getDeclaringClass());
        }
        return new MockNoType();
    }

    @Override
    public ElementKind getKind() {
        if (source instanceof Field) {
            Field field = (Field) source;
            if (field.isEnumConstant()) {
                return ElementKind.ENUM_CONSTANT;
            }
            return ElementKind.FIELD;
        } else if (source instanceof Parameter) {
            return ElementKind.PARAMETER;
        } else if (source instanceof Enum) {
            return ElementKind.ENUM_CONSTANT;
        }
        return ElementKind.OTHER;
    }

    @Override
    public Set<javax.lang.model.element.Modifier> getModifiers() {
        Set<javax.lang.model.element.Modifier> modifiers = new HashSet<>();

        if (source instanceof Member m) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod)) modifiers.add(javax.lang.model.element.Modifier.PUBLIC);
            if (Modifier.isPrivate(mod)) modifiers.add(javax.lang.model.element.Modifier.PRIVATE);
            if (Modifier.isProtected(mod)) modifiers.add(javax.lang.model.element.Modifier.PROTECTED);
            if (Modifier.isStatic(mod)) modifiers.add(javax.lang.model.element.Modifier.STATIC);
            if (Modifier.isFinal(mod)) modifiers.add(javax.lang.model.element.Modifier.FINAL);
            if (Modifier.isVolatile(mod)) modifiers.add(javax.lang.model.element.Modifier.VOLATILE);
            if (Modifier.isTransient(mod)) modifiers.add(javax.lang.model.element.Modifier.TRANSIENT);
        }
        
        return modifiers;
    }

    @Override
    public Element getEnclosingElement() {
        if (source instanceof Field) {
            return new MockTypeElement(((Field) source).getDeclaringClass());
        } else if (source instanceof Parameter) {
            return new MockExecutableElement(((Parameter) source).getDeclaringExecutable());
        } else if (source instanceof Enum) {
            return new MockTypeElement(((Enum<?>) source).getDeclaringClass());
        }
        return null;
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
        if (source instanceof Field field) {
            return Arrays.stream(field.getAnnotations())
                    .map(MockAnnotationMirror::new)
                    .toList();
        } else if (source instanceof Parameter parameter) {
            return Arrays.stream(parameter.getAnnotations())
                    .map(MockAnnotationMirror::new)
                    .toList();
        }
        return List.of();
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
        return List.of();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        if (source instanceof Field) {
            return ((Field) source).getAnnotation(annotationType);
        } else if (source instanceof Parameter) {
            return ((Parameter) source).getAnnotation(annotationType);
        }
        return null;
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        if (source instanceof Field) {
            return ((Field) source).getAnnotationsByType(annotationType);
        } else if (source instanceof Parameter) {
            return ((Parameter) source).getAnnotationsByType(annotationType);
        }
        return null;
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p) {
        return v.visitVariable(this, p);
    }

    @Override
    public String toString() {
        return source.toString();
    }
}