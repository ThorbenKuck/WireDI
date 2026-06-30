package com.wiredi.compiler.domain.annotations;

import com.wiredi.compiler.domain.annotations.identifiers.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.function.Predicate;

public interface AnnotationIdentifier {

    boolean matches(@NotNull AnnotationMirror annotationMirror);

    boolean supportsInheritance();

    @NotNull
    static PredicateAnnotationIdentifier matching(@NotNull Predicate<@NotNull AnnotationMirror> predicate) {
        return new PredicateAnnotationIdentifier(predicate);
    }

    @NotNull
    static QualifiedAnnotationName qualifiedName(@NotNull String name) {
        return new QualifiedAnnotationName(name);
    }

    @NotNull
    static SimpleAnnotationName simpleName(@NotNull String name) {
        return new SimpleAnnotationName(name);
    }

    @NotNull
    static <T extends Annotation> AnnotationType<T> of(@NotNull Class<T> annotationClass, @Nullable Types types, @Nullable Elements elements) {
        return new AnnotationType<>(annotationClass, types, elements);
    }

    @NotNull
    static <T extends Annotation> AnnotationType<T> of(@NotNull Class<T> annotationClass) {
        return new AnnotationType<>(annotationClass, null, null);
    }

    @NotNull
    static WildcardAnnotationName wildcard(@NotNull String name) {
        return new WildcardAnnotationName(name);
    }
}
