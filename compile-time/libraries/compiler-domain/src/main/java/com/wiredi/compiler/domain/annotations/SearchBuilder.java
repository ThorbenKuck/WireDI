package com.wiredi.compiler.domain.annotations;

import com.wiredi.compiler.domain.Annotations;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.function.Predicate;

public class SearchBuilder {

    private final SearchConfiguration configuration = new SearchConfiguration();
    @Nullable
    private final Types types;
    @Nullable
    private final Elements elements;

    public SearchBuilder(@Nullable Types types, @Nullable Elements elements) {
        this.types = types;
        this.elements = elements;
    }

    public SearchBuilder withInheritance(boolean supportInheritance) {
        configuration.supportInheritance(supportInheritance);
        return this;
    }

    public SearchBuilder withoutInheritance() {
        this.configuration.supportInheritance(false);
        return this;
    }

    public SearchBuilder includeJdkAnnotations() {
        this.configuration.ignoreJdkAnnotations(false);
        return this;
    }

    public SearchBuilder ignoreJdkAnnotations() {
        this.configuration.ignoreJdkAnnotations(true);
        return this;
    }

    public AnnotationSearch metaAnnotatedWith(Class<? extends Annotation> annotationType) {
        return new AnnotationSearch(AnnotationIdentifier.matching(it -> Annotations.isMetaAnnotatedWith(it.getAnnotationType().asElement(), annotationType)), configuration);

    }

    public AnnotationSearch by(Predicate<AnnotationMirror> predicate) {
        return new AnnotationSearch(AnnotationIdentifier.matching(predicate), configuration);
    }

    public <T extends Annotation> TypedAnnotationSearch<T> by(Class<T> annotationType) {
        return new TypedAnnotationSearch<>(AnnotationIdentifier.of(annotationType, types, elements), configuration);
    }

    public AnnotationSearch by(AnnotationIdentifier annotationIdentifier) {
        return new AnnotationSearch(annotationIdentifier, configuration);
    }

    /**
     * Search by annotation name (supports wildcards).
     */
    public AnnotationSearch byName(Class<? extends Annotation> type) {
        return new AnnotationSearch(AnnotationIdentifier.simpleName(type.getName()), configuration);
    }

    /**
     * Search by annotation name (supports wildcards).
     */
    public AnnotationSearch byName(String name) {
        return new AnnotationSearch(AnnotationIdentifier.simpleName(name), configuration);
    }

    /**
     * Search by annotation name with exact matching.
     */
    public AnnotationSearch byExactName(String name) {
        return new AnnotationSearch(AnnotationIdentifier.qualifiedName(name), configuration);
    }

    /**
     * Search by annotation name with wildcard pattern.
     */
    public AnnotationSearch byWildcard(String pattern) {
        return new AnnotationSearch(AnnotationIdentifier.wildcard(pattern), configuration);
    }

    /**
     * Search by annotation type (strongly typed).
     */
    public <T extends Annotation> TypedAnnotationSearch<T> byType(Class<T> annotationType) {
        return new TypedAnnotationSearch<>(AnnotationIdentifier.of(annotationType, types, elements), configuration);
    }
}