package com.wiredi.compiler.domain.annotations.identifiers;

import com.wiredi.compiler.domain.annotations.AnnotationIdentifier;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.AnnotationMirror;
import java.util.Objects;

public class QualifiedAnnotationName implements AnnotationIdentifier {

    private final String name;

    public QualifiedAnnotationName(String name) {
        this.name = name;
    }

    @Override
    public boolean matches(@NotNull AnnotationMirror mirror) {
        return Objects.equals(mirror.getAnnotationType().toString(), name);
    }

    public boolean matches(String annotationName) {
        return Objects.equals(name, annotationName);
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
        QualifiedAnnotationName that = (QualifiedAnnotationName) o;
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
