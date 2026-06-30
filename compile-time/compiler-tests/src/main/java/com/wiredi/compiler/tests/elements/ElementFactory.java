package com.wiredi.compiler.tests.elements;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Utility class for creating mock TypeElement and TypeMirror instances from Java Class objects.
 * This is useful for testing annotation processing code without needing a full compilation environment.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * // Create a TypeElement from a class
 * TypeElement element = MockElementFactory.createTypeElement(MyClass.class);
 *
 * // Create a TypeMirror from a class
 * TypeMirror type = MockElementFactory.createTypeMirror(String.class);
 *
 * // Use in tests
 * AnnotationSearch search = Annotations.search().byType(MyAnnotation.class);
 * Optional<MyAnnotation> annotation = search.findFirstIn(element);
 * }</pre>
 */
public class ElementFactory {

    private ElementFactory() {
        // Utility class, no instantiation
    }

    /**
     * Creates a TypeElement from a Java Class.
     *
     * @param clazz the class to create a TypeElement for
     * @return a mock TypeElement representing the class
     */
    public static TypeElement createTypeElement(Class<?> clazz) {
        return new MockTypeElement(clazz);
    }

    /**
     * Creates a TypeMirror from a Java Class.
     *
     * @param clazz the class to create a TypeMirror for
     * @return a mock TypeMirror representing the class
     */
    public static TypeMirror createTypeMirror(Class<?> clazz) {
        return new MockTypeMirror(clazz);
    }
}