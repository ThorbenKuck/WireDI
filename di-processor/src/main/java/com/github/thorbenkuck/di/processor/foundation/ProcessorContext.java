package com.github.thorbenkuck.di.processor.foundation;

import com.github.thorbenkuck.di.processor.AspectIgnoredAnnotations;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Objects;

public class ProcessorContext {

    private static final ThreadLocal<Types> types = new ThreadLocal<>();
    private static final ThreadLocal<Elements> elements = new ThreadLocal<>();
    private static final ThreadLocal<Filer> filer = new ThreadLocal<>();
    private static final ThreadLocal<AspectIgnoredAnnotations> aspectIgnoredAnnotations = new ThreadLocal<>();

    public static void update(ProcessingEnvironment processingEnvironment) {
        types.set(processingEnvironment.getTypeUtils());
        elements.set(processingEnvironment.getElementUtils());
        filer.set(processingEnvironment.getFiler());
        if(aspectIgnoredAnnotations.get() == null) {
            aspectIgnoredAnnotations.set(AspectIgnoredAnnotations.get());
        }
    }

    public static Types getTypes() {
        return Objects.requireNonNull(types.get());
    }

    public static Elements getElements() {
        return Objects.requireNonNull(elements.get());
    }

    public static Filer getFiler() {
        return Objects.requireNonNull(filer.get());
    }

    public static boolean isAnnotationIgnored(Element element) {
        return Objects.requireNonNull(aspectIgnoredAnnotations.get()).isIgnored(element);
    }

    public static boolean isAnnotationIgnored(TypeMirror typeMirror) {
        return Objects.requireNonNull(aspectIgnoredAnnotations.get()).isIgnored(typeMirror);
    }

    public static boolean isUsedForAop(AnnotationMirror annotationMirror) {
        return !isAnnotationIgnored(annotationMirror.getAnnotationType().asElement());
    }
}
