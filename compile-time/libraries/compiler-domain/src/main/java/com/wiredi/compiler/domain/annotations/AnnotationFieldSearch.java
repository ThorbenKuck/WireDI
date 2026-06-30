package com.wiredi.compiler.domain.annotations;

import com.wiredi.compiler.domain.AnnotationField;
import com.wiredi.logging.Logging;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import java.util.Map;
import java.util.Optional;

public class AnnotationFieldSearch {

    private final String fieldName;
    private final AnnotationSearch search;
    private static final Logging logger = Logging.getInstance(AnnotationFieldSearch.class);

    public AnnotationFieldSearch(String fieldName, AnnotationSearch search) {
        this.fieldName = fieldName;
        this.search = search;
    }

    public <T extends AnnotationValue> Optional<AnnotationField<T>> inMirror(AnnotationMirror mirror) {
        return findInElement(mirror.getAnnotationType().asElement());
    }

    public <T extends AnnotationValue> Optional<AnnotationField<T>> findInElement(Element element) {
        logger.info(() -> "Searching for field " + fieldName + " in " + element);
        return search.findFirstMirrorIn(element).map(annotationMirror -> {
            logger.info(() -> "Found annotation " + annotationMirror.getAnnotationType().asElement().getSimpleName() + " in " + element + ". Searching for field " + fieldName + " in " + annotationMirror.getElementValues());
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
                logger.info(() -> "Found field " + entry.getKey().getSimpleName() + " with value " + entry.getValue() + ". Checking against " + fieldName);
                if (fieldName.equals(entry.getKey().getSimpleName().toString())) {
                    return AnnotationField.of(annotationMirror, fieldName, (T) entry.getValue());
                }
            }

            logger.info(() -> "Field " + fieldName + " not found in " + element);
            return null;
        });
    }
}
