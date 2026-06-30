package com.wiredi.compiler.tests.elements;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Mock implementation of AnnotationMirror for testing purposes.
 */
public class MockAnnotationMirror implements AnnotationMirror {
    private final Annotation annotation;
    private final DeclaredType annotationType;
    private Map<ExecutableElement, AnnotationValue> elementValues;

    public MockAnnotationMirror(Annotation annotation) {
        this.annotation = annotation;
        this.annotationType = new MockTypeMirror(annotation.annotationType());
    }

    @Override
    public DeclaredType getAnnotationType() {
        return annotationType;
    }

    @Override
    public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValues() {
        if (elementValues == null) {
            elementValues = new LinkedHashMap<>();
            Method[] methods = annotation.annotationType().getDeclaredMethods();

            for (Method method : methods) {
                try {
                    method.trySetAccessible();
                    Object value = method.invoke(annotation);
                    ExecutableElement executableElement = new MockExecutableElement(method);
                    AnnotationValue annotationValue = new MockAnnotationValue(value);
                    elementValues.put(executableElement, annotationValue);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalStateException("Unable to read " + method + " from " + annotation.annotationType(), e);
                }
            }
        }
        return elementValues;
    }

    public Annotation getAnnotationInstance() {
        return annotation;
    }

    @Override
    public String toString() {
        return annotation.toString();
    }
}