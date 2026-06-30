package com.wiredi.compiler.domain.annotations;

import javax.lang.model.element.AnnotationMirror;

public class SearchConfiguration {

    private boolean supportInheritance = true;
    private boolean ignoreJdkAnnotations = true;

    public boolean supportInheritance() {
        return supportInheritance;
    }

    public SearchConfiguration supportInheritance(boolean supportInheritance) {
        this.supportInheritance = supportInheritance;
        return this;
    }

    public boolean ignoreJdkAnnotations() {
        return ignoreJdkAnnotations;
    }

    public SearchConfiguration ignoreJdkAnnotations(boolean respectJdkAnnotations) {
        this.ignoreJdkAnnotations = respectJdkAnnotations;
        return this;
    }

    public boolean skip(AnnotationMirror annotationMirror) {
        if (isJdkAnnotation(annotationMirror)) {
            return ignoreJdkAnnotations;
        }

        return false;
    }

    private boolean isJdkAnnotation(AnnotationMirror annotationMirror) {
        String name = annotationMirror.getAnnotationType().asElement().toString();
        return name.startsWith("java.lang.annotation") || name.startsWith("kotlin.");
    }
}
