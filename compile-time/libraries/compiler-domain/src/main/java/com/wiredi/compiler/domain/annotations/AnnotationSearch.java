package com.wiredi.compiler.domain.annotations;

import com.wiredi.compiler.domain.Annotations;
import com.wiredi.logging.Logging;
import com.wiredi.runtime.domain.annotations.AnnotationMetadata;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Untyped annotation search that returns AnnotationMirror or AnnotationMetadata.
 * This is the base class for weak-typed searches.
 */
public class AnnotationSearch {

    protected final AnnotationIdentifier identifier;
    protected final SearchConfiguration configuration;

    private static final Logging logger = Logging.getInstance(AnnotationSearch.class);

    public AnnotationSearch(AnnotationIdentifier identifier, SearchConfiguration configuration) {
        this.identifier = identifier;
        this.configuration = configuration;
    }

    public AnnotationFieldSearch field(String fieldName) {
        return new AnnotationFieldSearch(fieldName, this);
    }

    /**
     * Get the first matching AnnotationMirror.
     */
    public Optional<AnnotationMirror> findFirstMirrorIn(AnnotationMirror mirror) {
        return findFirstMirrorIn(mirror.getAnnotationType().asElement());
    }

    /**
     * Get all matching AnnotationMirrors.
     */
    public List<AnnotationMirror> findAllMirrorsIn(AnnotationMirror mirror) {
        return findAllMirrorsIn(mirror.getAnnotationType().asElement());
    }

    /**
     * Get the first matching AnnotationMetadata.
     */
    public Optional<AnnotationMetadata> findFirstMetadataIn(AnnotationMirror mirror) {
        return findFirstMetadataIn(mirror.getAnnotationType().asElement());
    }

    /**
     * Get all matching AnnotationMetadata.
     */
    public List<AnnotationMetadata> findAllMetadataIn(AnnotationMirror mirror) {
        return findAllMetadataIn(mirror.getAnnotationType().asElement());
    }

    /**
     * Get the first matching AnnotationMirror.
     */
    public Optional<AnnotationMirror> findFirstMirrorIn(Element element) {
        return Optional.ofNullable(searchMirror(element));
    }

    /**
     * Get all matching AnnotationMirrors.
     */
    public List<AnnotationMirror> findAllMirrorsIn(Element element) {
        return searchMirrors(element);
    }

    /**
     * Get the first matching AnnotationMetadata.
     */
    public Optional<AnnotationMetadata> findFirstMetadataIn(Element element) {
        return findFirstMirrorIn(element).map(AnnotationMetadata::of);
    }

    /**
     * Get all matching AnnotationMetadata.
     */
    public List<AnnotationMetadata> findAllMetadataIn(Element element) {
        return findAllMirrorsIn(element).stream()
                .map(AnnotationMetadata::of)
                .toList();
    }

    /**
     * Check if the annotation is present.
     */
    public boolean isPresentIn(Element element) {
        return findFirstMirrorIn(element).isPresent();
    }

    /**
     * Check if the annotation is present.
     */
    public boolean isPresentIn(AnnotationMirror mirror) {
        return isPresentIn(mirror.getAnnotationType().asElement());
    }

    /**
     * Check if the annotation is present.
     */
    public boolean isNotPresentIn(Element element) {
        return findFirstMirrorIn(element).isEmpty();
    }

    /**
     * Check if the annotation is present.
     */
    public boolean isNotPresentIn(AnnotationMirror mirror) {
        return isNotPresentIn(mirror.getAnnotationType().asElement());
    }

    protected List<AnnotationMirror> searchMirrors(Element element) {
        List<AnnotationMirror> results = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        searchMirrors(element, results, visited, configuration.supportInheritance(), !configuration.ignoreJdkAnnotations());
        return results;
    }

    protected List<AnnotationMirror> searchMirrors(AnnotationMirror mirror) {
        return searchMirrors(mirror.getAnnotationType().asElement());
    }

    private void searchMirrors(Element currentElement, List<AnnotationMirror> results, Set<String> visited, boolean includeInherited, boolean ignoreJdkAnnotations) {
        // Prevent infinite recursion by tracking visited elements
        String elementKey = currentElement.toString();
        if (!visited.add(elementKey)) {
            return;
        }

        // Direct search
        for (AnnotationMirror mirror : currentElement.getAnnotationMirrors()) {
            if (Annotations.isJdkAnnotation(mirror) && ignoreJdkAnnotations) {
                continue;
            }

            if (identifier.matches(mirror)) {
                logger.debug(() -> "Matched identifier " + identifier + " in " + currentElement + " (" + mirror + ")");
                results.add(mirror);
            }

            // Meta-annotation search
            if (includeInherited && identifier.supportsInheritance()) {
                searchMirrors(mirror.getAnnotationType().asElement(), results, visited, true, ignoreJdkAnnotations);
            }
        }
    }

    @Nullable
    protected AnnotationMirror searchMirror(Element element) {
        Set<String> visited = new HashSet<>();
        return searchMirror(element, visited, configuration.supportInheritance(), !configuration.ignoreJdkAnnotations());
    }

    @Nullable
    protected AnnotationMirror searchMirror(AnnotationMirror mirror) {
        return searchMirror(mirror.getAnnotationType().asElement());
    }

    /**
     * Optimized search for "first hit"
     */
    @Nullable
    private AnnotationMirror searchMirror(Element currentElement, Set<String> visited, boolean includeInherited, boolean ignoreJdkAnnotations) {
        // Prevent infinite recursion by tracking visited elements
        String elementKey = currentElement.toString();
        if (!visited.add(elementKey)) {
            return null;
        }

        // Direct search
        for (AnnotationMirror mirror : currentElement.getAnnotationMirrors()) {
            if (Annotations.isJdkAnnotation(mirror) && ignoreJdkAnnotations) {
                continue;
            }

            if (identifier.matches(mirror)) {
                logger.debug(() -> "Matched identifier " + identifier + " in " + currentElement + " (" + mirror + "). Returning Mirror");
                return mirror;
            }

            // Meta-annotation search
            if (includeInherited && identifier.supportsInheritance()) {
                AnnotationMirror nestedHit = searchMirror(mirror.getAnnotationType().asElement(), visited, true, ignoreJdkAnnotations);
                if (nestedHit != null) {
                    return nestedHit;
                }
            }
        }

        return null;
    }
}
