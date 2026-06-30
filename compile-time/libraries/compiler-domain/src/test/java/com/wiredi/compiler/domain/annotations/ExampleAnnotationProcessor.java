package com.wiredi.compiler.domain.annotations;

import com.wiredi.compiler.domain.Annotations;
import org.opentest4j.AssertionFailedError;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class ExampleAnnotationProcessor extends AbstractProcessor {

    private static final AnnotationSearch exampleAnnotation = Annotations.search().by(ExampleAnnotation.class);
    private static final AnnotationSearch inheritedAnnotation = Annotations.search().by(InheritedAnnotation.class);
    private final Results results = new Results()
            .register(ExampleAnnotation.class)
            .register(InheritedAnnotation.class);

    public class Results {
        private final Map<Class<? extends Annotation>, AnnotationResult> map = new HashMap<>();

        private Results register(Class<? extends Annotation> clazz, String... keys) {
            AnnotationResult annotationResult = map.computeIfAbsent(clazz, it -> new AnnotationResult(this));
            for (String key : keys) {
                annotationResult.register(key);
            }
            return this;
        }

        private Results set(Class<? extends Annotation> clazz, String key, boolean value) {
            map.get(clazz).set(key, value);
            return this;
        }

        public AnnotationResult verify(Class<? extends Annotation> clazz) {
            AnnotationResult annotationResult = map.get(clazz);
            if (annotationResult == null) {
                throw new AssertionFailedError("The annotation " + clazz.getName() + " was not used in this test.");
            }

            return annotationResult;
        }

        public Results verify(Class<? extends Annotation> clazz, Consumer<AnnotationResult> consumer) {
            AnnotationResult annotationResult = map.get(clazz);
            if (annotationResult == null) {
                throw new AssertionFailedError("The annotation " + clazz.getName() + " was not used in this test.");
            }
            consumer.accept(annotationResult);

            return this;
        }
    }

    public class AnnotationResult {

        private final Results parent;
        private final Map<String, Object> map = new HashMap<>();

        public AnnotationResult(Results parent) {
            this.parent = parent;
        }

        private AnnotationResult register(String key) {
            map.put(key, false);
            return this;
        }

        private AnnotationResult set(String key, Object value) {
            map.put(key, value);
            return this;
        }

        public AnnotationResult wasFoundOn(String key, String... additional) {
            Object actual = map.remove(key);
            assertMatching(key, true, actual);
            for (String nextKey : additional) {
                actual = map.remove(nextKey);
                assertMatching(nextKey, true, actual);
            }
            return this;
        }

        public AnnotationResult wasNotFoundOn(String key, String... additional) {
            Object actual = map.remove(key);
            assertMatching(key, false, actual);
            for (String nextKey : additional) {
                actual = map.remove(nextKey);
                assertMatching(nextKey, false, actual);
            }
            return this;
        }

        public Results assertAllChecked() {
            if (!map.isEmpty()) {
                throw new AssertionFailedError("The following fields have been found as well: " + map.entrySet().stream().map(it -> it.getKey() + ": " + it.getValue()).toList());
            }

            return parent;
        }

        private void assertMatching(String key, Object expected, Object actual) {
            if (actual == null) {
                throw new AssertionFailedError("The field " + key + " was not used.", true, null);
            }
            if (actual != expected) {
                throw new AssertionFailedError("Expected " + key + " was checked differently.", expected, actual);
            }
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(TestCompile.class.getName());
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        roundEnv.getElementsAnnotatedWith(TestCompile.class).forEach(e -> {
            results.set(ExampleAnnotation.class, "class " + e.getSimpleName().toString(), exampleAnnotation.isPresentIn(e));
            results.set(InheritedAnnotation.class, "class " + e.getSimpleName().toString(), inheritedAnnotation.isPresentIn(e));
            e.getEnclosedElements().forEach(innerElement -> {
                String prefix = null;

                if (innerElement instanceof VariableElement) {
                    prefix = "field " + innerElement.getSimpleName();
                } else if (innerElement instanceof ExecutableElement) {
                    prefix = "method " + innerElement.getSimpleName();
                } else if (innerElement instanceof TypeElement) {
                    prefix = "inner-class " + innerElement.getSimpleName();
                }

                if (prefix != null) {
                    results.set(ExampleAnnotation.class, prefix, exampleAnnotation.isPresentIn(innerElement));
                    results.set(InheritedAnnotation.class, prefix, inheritedAnnotation.isPresentIn(innerElement));
                }
            });
        });

        return true;
    }

    public Results getResults() {
        return results;
    }
}
