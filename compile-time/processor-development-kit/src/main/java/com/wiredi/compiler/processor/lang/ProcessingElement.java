package com.wiredi.compiler.processor.lang;

import com.wiredi.compiler.domain.Annotations;
import com.wiredi.runtime.domain.annotations.AnnotationMetadata;
import com.wiredi.runtime.values.Value;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;

public class ProcessingElement {

    private final Element element;
    private final Annotations annotations;
    private final Class<? extends Annotation> matchedAnnotationType;
    private final Value<AnnotationMirror> annotationMirror;
    private final Value<Annotation> annotation;
    private final Value<AnnotationMetadata> annotationMetadata;

    public ProcessingElement(Element element, Annotations annotations, Class<? extends Annotation> matchedAnnotationType) {
        this.element = element;
        this.annotations = annotations;
        this.matchedAnnotationType = matchedAnnotationType;
        this.annotationMirror = Value.lazy(() -> annotations.getAnnotationMirror(element, matchedAnnotationType));
        this.annotation = Value.lazy(() -> Annotations.getAnnotation(element, matchedAnnotationType)
                .orElseThrow(() -> new IllegalStateException("Annotation not found. However this is possible."))
        );
        this.annotationMetadata = Value.lazy(() -> AnnotationMetadata.of(annotationMirror.get()));
    }

    public AnnotationMirror annotationMirror() {
        return annotationMirror.get();
    }
    
    public Annotation annotation() {
        return annotation.get() ;
    }

    public AnnotationMetadata annotationMetadata() {
        return annotationMetadata.get();
    }

    public Element element() {
        return element;
    }

    public Annotations annotations() {
        return annotations;
    }

    public Class<? extends Annotation> matchedAnnotationType() {
        return matchedAnnotationType;
    }
}
