package com.wiredi.compiler.domain.annotations.identifiers;

import com.wiredi.compiler.domain.annotations.AnnotationIdentifier;
import com.wiredi.runtime.lang.WildcardMatcher;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.AnnotationMirror;
import java.util.Objects;

/**
 * Identifies annotations by wildcard pattern.
 * Dots are completely ignored for matching - package boundaries are irrelevant.
 * Example: "*Wire*" matches "Wire", "com.wiredi.annotations.Wire", "MyWireClass", etc.
 */
public class WildcardAnnotationName implements AnnotationIdentifier {

    private final String name;
    private final WildcardMatcher matcher;

    public WildcardAnnotationName(String name) {
        this.name = name;
        this.matcher = WildcardMatcher.compile(name);
    }

    public static WildcardAnnotationName of(String name) {
        return new WildcardAnnotationName(name);
    }

    @Override
    public boolean matches(@NotNull AnnotationMirror annotationMirror) {
        String qualifiedName = annotationMirror.getAnnotationType().toString();

        return matcher.matches(qualifiedName);
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
        WildcardAnnotationName that = (WildcardAnnotationName) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "WildcardAnnotationName{" + name + '}';
    }
}
