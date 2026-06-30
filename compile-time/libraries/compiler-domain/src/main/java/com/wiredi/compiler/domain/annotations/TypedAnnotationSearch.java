package com.wiredi.compiler.domain.annotations;

import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.domain.annotations.identifiers.AnnotationType;
import com.wiredi.logging.Logging;
import com.wiredi.runtime.domain.annotations.AnnotationExcerpt;
import com.wiredi.runtime.domain.annotations.AnnotationMetadata;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Strongly typed annotation search that extends MirroredAnnotationSearch.
 */
public class TypedAnnotationSearch<T extends Annotation> extends AnnotationSearch {

    private static final Logging logger = Logging.getInstance(TypedAnnotationSearch.class);
    private final AnnotationType<T> typedIdentifier;

    public TypedAnnotationSearch(AnnotationType<T> identifier, SearchConfiguration configuration) {
        super(identifier, configuration);
        this.typedIdentifier = identifier;
    }

    /**
     * Get the first matching annotation instance.
     */
    public Optional<T> findFirstIn(Element element) {
        return findFirstMirrorIn(element).map(this::extractAnnotation);
    }

    /**
     * Get the first matching annotation instance.
     */
    public Optional<T> findFirstIn(AnnotationMirror mirror) {
        return findFirstIn(mirror.getAnnotationType().asElement());
    }

    /**
     * Get all matching annotation instances.
     */
    public List<T> findAllIn(Element element) {
        return findAllMirrorsIn(element).stream()
                .map(this::extractAnnotation)
                .toList();
    }

    /**
     * Get all matching annotation instances.
     */
    public List<T> findAllIn(AnnotationMirror mirror) {
        return findAllIn(mirror.getAnnotationType().asElement());
    }

    /**
     * Get the first matching AnnotationExcerpt.
     */
    public Optional<AnnotationExcerpt<T>> findFirstExcerptIn(Element element) {
        return findFirstMirrorIn(element).map(mirror -> {
            T annotation = extractAnnotation(mirror);
            AnnotationMetadata metadata = AnnotationMetadata.of(mirror);
            return new AnnotationExcerpt<>(annotation, mirror, metadata);
        });
    }

    /**
     * Get the first matching AnnotationExcerpt.
     */
    public Optional<AnnotationExcerpt<T>> findFirstExcerptIn(AnnotationMirror mirror) {
        return findFirstExcerptIn(mirror.getAnnotationType().asElement());
    }

    /**
     * Get all matching AnnotationExcerpts.
     */
    public List<AnnotationExcerpt<T>> findAllExcerptsIn(Element element) {
        return findAllMirrorsIn(element).stream()
                .map(mirror -> {
                    T annotation = extractAnnotation(mirror);
                    AnnotationMetadata metadata = AnnotationMetadata.of(mirror);
                    return new AnnotationExcerpt<>(annotation, mirror, metadata);
                })
                .toList();
    }

    /**
     * Get all matching AnnotationExcerpts.
     */
    public List<AnnotationExcerpt<T>> findAllExcerptsIn(AnnotationMirror mirror) {
        return findAllExcerptsIn(mirror.getAnnotationType().asElement());
    }

    @SuppressWarnings("unchecked")
    private T extractAnnotation(AnnotationMirror mirror) {
        logger.info(() -> "Extracting annotation from mirror: " + mirror);
        // Try to extract from mock implementation first (for testing)
        if (mirror instanceof com.wiredi.compiler.tests.elements.MockAnnotationMirror mockMirror) {
            Annotation annotation = mockMirror.getAnnotationInstance();
            if (typedIdentifier.getType().isInstance(annotation)) {
                return (T) annotation;
            }
        }

        Class<T> annotationType = typedIdentifier.getType();
        return Annotations.proxy(mirror, annotationType);
    }

}
