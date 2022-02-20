package com.github.thorbenkuck.di.processor.foundation;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Objects;

public class ProcessorContext {

    private static ThreadLocal<Types> types = new ThreadLocal<>();
    private static ThreadLocal<Elements> elements = new ThreadLocal<>();
    private static ThreadLocal<Filer> filer = new ThreadLocal<>();

    public static void update(ProcessingEnvironment processingEnvironment) {
        types.set(processingEnvironment.getTypeUtils());
        elements.set(processingEnvironment.getElementUtils());
        filer.set(processingEnvironment.getFiler());
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
}
