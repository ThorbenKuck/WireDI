package com.wiredi.compiler.logger.slf4j;

import com.wiredi.runtime.lang.ThrowingRunnable;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.function.Consumer;

public class MessagerContext {

    private static final ThreadLocal<Instance> context = new ThreadLocal<>();

    public static void runNested(Consumer<Instance> runnable) {
        Instance previousInstance = context.get();
        Instance newInstance;
        if (previousInstance != null) {
            newInstance = previousInstance.copy();
        } else {
            newInstance = new Instance();
        }

        context.set(newInstance);
        try {
            runnable.accept(newInstance);
        } finally {
            if (previousInstance == null) {
                context.remove();
            } else {
                context.set(previousInstance);
            }
        }
    }

    public static void runIsolated(Consumer<Instance> runnable) {
        Instance previousInstance = context.get();
        Instance newInstance = new Instance();
        context.set(newInstance);
        try {
            runnable.accept(newInstance);
        } finally {
            if (previousInstance == null) {
                context.remove();
            } else {
                context.set(previousInstance);
            }
        }
    }

    public static Instance get() {
        Instance existingInstance = context.get();
        if (existingInstance == null) {
            Instance instance = new Instance();
            context.set(instance);
            return instance;
        }
        return existingInstance;
    }

    private static void set(@Nullable Instance instance) {
        if (instance == null) {
            context.remove();
        } else {
            context.set(instance);
        }
    }

    @Nullable
    private static Instance tryGet() {
        return context.get();
    }

    public static class Instance {
        private Element element;
        private AnnotationMirror annotationMirror;
        private AnnotationValue annotationValue;
        private Class<? extends Annotation> annotationType;

        private Instance() {
        }

        private Instance(Instance instance) {
            this.element = instance.element;
            this.annotationMirror = instance.annotationMirror;
            this.annotationValue = instance.annotationValue;
            this.annotationType = instance.annotationType;
        }

        public Class<? extends Annotation> getAnnotationType() {
            return annotationType;
        }

        public Instance setAnnotationType(Class<? extends Annotation> annotationType) {
            this.annotationType = annotationType;
            return this;
        }

        public Element getElement() {
            return element;
        }

        public Instance setElement(Element element) {
            this.element = element;
            return this;
        }

        public AnnotationMirror getAnnotationMirror() {
            return annotationMirror;
        }

        public Instance setAnnotationMirror(AnnotationMirror annotationMirror) {
            this.annotationMirror = annotationMirror;
            return this;
        }

        public AnnotationValue getAnnotationValue() {
            return annotationValue;
        }

        public Instance setAnnotationValue(AnnotationValue annotationValue) {
            this.annotationValue = annotationValue;
            return this;
        }

        public Instance copy() {
            return new Instance(this);
        }

        public <T extends Throwable> void run(ThrowingRunnable<T> runnable) throws T {
            Instance previousInstance = tryGet();
            try {
                set(this);
                runnable.run();
            } finally {
                set(previousInstance);
            }
        }
    }
}
