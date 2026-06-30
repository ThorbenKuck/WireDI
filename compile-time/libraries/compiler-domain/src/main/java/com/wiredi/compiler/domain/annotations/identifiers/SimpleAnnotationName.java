package com.wiredi.compiler.domain.annotations.identifiers;

import com.wiredi.compiler.domain.annotations.AnnotationIdentifier;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.AnnotationMirror;
import java.util.Objects;

public class SimpleAnnotationName implements AnnotationIdentifier {

    private final String name;

    public SimpleAnnotationName(String name) {
        this.name = name;
    }

    public static SimpleAnnotationName of(String name) {
        return new SimpleAnnotationName(name);
    }

    @Override
    public boolean matches(@NotNull AnnotationMirror mirror) {
        String qualifiedName = mirror.getAnnotationType().toString();
        String simpleName = mirror.getAnnotationType().asElement().getSimpleName().toString();

        return matches(qualifiedName) || matches(simpleName);
    }

    public boolean matches(String annotationName) {
        // Match exactly, or match if the annotation name ends with this simple name
        if (Objects.equals(name, annotationName)) {
            return true;
        }

        // Check if it matches the simple name (e.g., "Wire" matches "com.wiredi.annotations.Wire")
        String simpleName = getSimpleName(annotationName);
        return Objects.equals(name, simpleName);
    }

    private String getSimpleName(String qualifiedName) {
        int lastDot = qualifiedName.lastIndexOf('.');
        return lastDot >= 0 ? qualifiedName.substring(lastDot + 1) : qualifiedName;
    }

    @Override
    public boolean supportsInheritance() {
        return false; // Name-based matching doesn't support inheritance by default
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleAnnotationName that = (SimpleAnnotationName) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "ExactAnnotationName{" + name + '}';
    }
}
