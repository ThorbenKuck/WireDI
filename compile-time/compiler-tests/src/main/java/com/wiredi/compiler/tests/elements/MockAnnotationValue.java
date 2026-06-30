package com.wiredi.compiler.tests.elements;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock implementation of AnnotationValue for testing purposes.
 */
public class MockAnnotationValue implements AnnotationValue {
    private final Object value;

    public MockAnnotationValue(Object value) {
        this.value = value;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p) {
        if (value instanceof String) {
            return v.visitString((String) value, p);
        } else if (value instanceof Integer) {
            return v.visitInt((Integer) value, p);
        } else if (value instanceof Boolean) {
            return v.visitBoolean((Boolean) value, p);
        } else if (value instanceof Long) {
            return v.visitLong((Long) value, p);
        } else if (value instanceof Float) {
            return v.visitFloat((Float) value, p);
        } else if (value instanceof Double) {
            return v.visitDouble((Double) value, p);
        } else if (value instanceof Character) {
            return v.visitChar((Character) value, p);
        } else if (value instanceof Byte) {
            return v.visitByte((Byte) value, p);
        } else if (value instanceof Short) {
            return v.visitShort((Short) value, p);
        } else if (value instanceof Class) {
            return v.visitType(new MockTypeMirror((Class<?>) value), p);
        } else if (value instanceof Enum) {
            return v.visitEnumConstant(new MockVariableElement((Enum<?>) value), p);
        } else if (value != null && value.getClass().isArray()) {
            // Handle array values - convert array to List<AnnotationValue>
            List<AnnotationValue> arrayValues = new ArrayList<>();
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                Object element = Array.get(value, i);
                arrayValues.add(new MockAnnotationValue(element));
            }
            return v.visitArray(arrayValues, p);
        } else if (value instanceof java.lang.annotation.Annotation) {
            // Handle nested annotations
            return v.visitAnnotation(new MockAnnotationMirror((java.lang.annotation.Annotation) value), p);
        }
        return v.visitUnknown(this, p);
    }

    @Override
    public String toString() {
        if (value == null) {
            return "null";
        }

        if (value.getClass().isArray()) {
            // Pretty print arrays
            StringBuilder sb = new StringBuilder("[");
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                if (i > 0) sb.append(", ");
                Object element = Array.get(value, i);
                if (element instanceof String) {
                    sb.append('"').append(element).append('"');
                } else if (element instanceof Class) {
                    sb.append(((Class<?>) element).getName()).append(".class");
                } else {
                    sb.append(element);
                }
            }
            sb.append("]");
            return sb.toString();
        }

        if (value instanceof String) {
            return '"' + value.toString() + '"';
        }

        if (value instanceof Class) {
            return ((Class<?>) value).getName() + ".class";
        }

        return value.toString();
    }
}