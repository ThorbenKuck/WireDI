package com.wiredi.compiler.processor.lang;

import java.lang.annotation.Annotation;
import java.util.List;

public interface AnnotationProcessorSubroutine {

    void handle(ProcessingElement element);

    List<Class<? extends Annotation>> targetAnnotations();

    static AnnotationProcessorSubroutine provider() {
        throw new UnsupportedOperationException("This processor is not a provider");
    }

    /**
     * This method can be overwritten, to be informed once the processor is initialized
     */
    default void doInitialization() {
    }

    /**
     * This method can be overwritten, to be informed of the processing over round
     */
    default void processingOver() {
        // NoOp, override to change behavior
    }
}
