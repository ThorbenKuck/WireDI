package com.wiredi.compiler.domain;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;

public record AnnotatedElement<A extends Annotation, T extends Element>(
        A annotation,
        T element
) {
}
