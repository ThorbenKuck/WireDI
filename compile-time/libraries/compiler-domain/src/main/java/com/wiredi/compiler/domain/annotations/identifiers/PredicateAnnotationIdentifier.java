package com.wiredi.compiler.domain.annotations.identifiers;

import com.wiredi.compiler.domain.annotations.AnnotationIdentifier;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.AnnotationMirror;
import java.util.function.Predicate;

public class PredicateAnnotationIdentifier implements AnnotationIdentifier {

    @NotNull
    private final Predicate<@NotNull AnnotationMirror> predicate;

    public PredicateAnnotationIdentifier(@NotNull Predicate<AnnotationMirror> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean matches(@NotNull AnnotationMirror annotationMirror) {
        return predicate.test(annotationMirror);
    }

    @Override
    public boolean supportsInheritance() {
        return false;
    }
}
