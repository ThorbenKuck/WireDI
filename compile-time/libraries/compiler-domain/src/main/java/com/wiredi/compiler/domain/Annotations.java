package com.wiredi.compiler.domain;

import com.wiredi.compiler.Injector;
import com.wiredi.compiler.domain.annotations.*;
import com.wiredi.logging.Logging;
import com.wiredi.runtime.domain.annotations.*;
import jakarta.inject.Inject;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Annotations {

    private static final Logging logger = Logging.getInstance(Annotations.class);
    private final ExtractionEnvironment extractionEnvironment = new ExtractionEnvironmentImplementation(this);
    @Nullable
    private static Types types;
    @Nullable
    private static Elements elements;
    @Inject
    private Injector injector;

    public static void init(ProcessingEnvironment processingEnv) {
        Annotations.types = processingEnv.getTypeUtils();
        Annotations.elements = processingEnv.getElementUtils();
    }

    public static boolean isJdkAnnotation(Element element) {
        if (element.getKind() != ElementKind.ANNOTATION_TYPE) {
            return false;
        }
        return element.toString().startsWith("java.lang.") || element.toString().startsWith("kotlin");
    }

    public static boolean isJdkAnnotation(AnnotationMirror annotationMirror) {
        return isJdkAnnotation(annotationMirror.getAnnotationType().asElement());
    }

    public static boolean isNotJdkAnnotation(Element element) {
        return !isJdkAnnotation(element);
    }

    public static boolean isNotJdkAnnotation(AnnotationMirror annotationMirror) {
        return !isJdkAnnotation(annotationMirror);
    }

    public static <T extends Annotation, S extends Class<?>> S extractClass(T annotation, Function<T, S> function) {
        try {
            return function.apply(annotation);
        } catch (MirroredTypeException e) {
            String qualifiedNameFromTypeMirror = getQualifiedNameFromTypeMirror(e.getTypeMirror());
            if (qualifiedNameFromTypeMirror == null) {
                logger.error(() -> "Could not extract class from " + e.getTypeMirror());
                throw new IllegalStateException("Could not extract class from " + e.getTypeMirror());
            }
            try {
                return (S) Class.forName(qualifiedNameFromTypeMirror);
            } catch (ClassNotFoundException ex) {
                logger.info(() -> "Could not find class " + qualifiedNameFromTypeMirror);
                throw new IllegalStateException("Could not find class " + qualifiedNameFromTypeMirror, ex);
            }
        }
    }

    public static <T extends Annotation> TypeMirror extractType(T annotation, Function<T, Type> function) {
        try {
            function.apply(annotation);
            throw new IllegalStateException("Impossible");
        } catch (MirroredTypeException e) {
            return e.getTypeMirror();
        }
    }

    public static boolean isAnnotatedWith(Element element, Class<? extends Annotation> annotation) {
        return getAnnotation(element, annotation).isPresent();
    }

    public static <T extends Annotation> Optional<T> getAnnotation(AnnotationMirror annotationMirror, Class<T> annotation) {
        return Optional.ofNullable(annotationMirror.getAnnotationType().asElement().getAnnotation(annotation))
                .or(() -> getInheritedFrom(annotationMirror.getAnnotationType().asElement(), annotation));
    }

    public static <T extends Annotation> Optional<T> getAnnotation(Element element, Class<T> annotation) {
        return Optional.ofNullable(element.getAnnotation(annotation))
                .or(() -> getInheritedFrom(element, annotation));
    }

    public static <T extends Annotation> Optional<T> getInheritedFrom(Element element, Class<T> annotation) {
        if (!annotation.isAnnotationPresent(Inherited.class)) {
            return Optional.empty();
        }

        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            if (isNotJdkAnnotation(annotationMirror)) {
                if (annotationMirror.getAnnotationType().asElement() == element) {
                    // Circuit break to prevent stack overflow
                    return Optional.empty();
                }
                Optional<T> from = getAnnotation(annotationMirror.getAnnotationType().asElement(), annotation);
                if (from.isPresent()) {
                    return from;
                }
            }
        }

        return Optional.empty();
    }

    public static boolean hasByName(Element element, Class<? extends Annotation> annotation) {
        if (annotation.getAnnotation(Inherited.class) != null) {
            return hasByNameInherited(element, annotation);
        }
        return hasByName(element, annotation.getSimpleName());
    }

    public static boolean hasByName(Element element, String name) {
        return element.getAnnotationMirrors()
                .stream()
                .anyMatch(it -> Objects.equals(it.getAnnotationType().asElement().getSimpleName().toString(), name));
    }

    public static boolean hasByNameInherited(Element element, Class<? extends Annotation> annotation) {
        return hasByNameInherited(element, annotation.getSimpleName());
    }

    public static boolean hasByNameInherited(Element element, String name) {
        if (hasByName(element, name)) {
            return true;
        }
        return element.getAnnotationMirrors()
                .stream()
                .anyMatch(it -> hasByName(it.getAnnotationType(), name));
    }

    public static boolean hasByName(DeclaredType annotation, String name) {
        if (isJdkAnnotation(annotation.asElement())) {
            return false;
        }
        return annotation.getAnnotationMirrors()
                .stream().anyMatch(it -> hasByNameInherited(it.getAnnotationType().asElement(), name));
    }

    public static Stream<? extends AnnotationMirror> findAll(Element element, Predicate<AnnotationMirror> include) {
        return element.getAnnotationMirrors()
                .stream()
                .filter(include);
    }

    public static boolean isMetaAnnotatedWith(Element element, Class<? extends Annotation> annotation) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            if (Objects.equals(annotationMirror.getAnnotationType().asElement().toString(), annotation.getName())) {
                if (isJdkAnnotation(annotationMirror)) {
                    continue;
                }

                return true;
            }
        }

        return false;
    }

    @Nullable
    public static String getQualifiedNameFromTypeMirror(TypeMirror typeMirror) {
        if (typeMirror.getKind() != TypeKind.DECLARED) return null;
        DeclaredType declaredType = (DeclaredType) typeMirror;
        Element element = declaredType.asElement();
        if (!(element instanceof TypeElement)) return null;
        return ((TypeElement) element).getQualifiedName().toString();
    }

    // ########## Non static fields ###########
    public <T extends Annotation> List<AnnotationExcerpt<T>> findAll(Class<T> annotationType, Element element) {
        List<AnnotationExcerpt<T>> result = new ArrayList<>();
        boolean inheritable = annotationType.isAnnotationPresent(Inherited.class);

        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            if (isJdkAnnotation(annotationMirror)) {
                continue;
            }

            if (inheritable && isMetaAnnotatedWith(annotationMirror.getAnnotationType().asElement(), annotationType)) {
                T annotation = annotationMirror.getAnnotationType().asElement().getAnnotation(annotationType);
                result.add(new AnnotationExcerpt<>(annotation, annotationMirror, getAnnotationMetadata(element, annotationMirror)));
            }

            if (Objects.equals(annotationMirror.getAnnotationType().asElement().toString(), annotationType.getName())) {
                T annotation = element.getAnnotation(annotationType);
                result.add(new AnnotationExcerpt<>(annotation, annotationMirror, getAnnotationMetadata(element, annotationMirror)));
            }
        }

        return result;
    }

    private AnnotationMetadata getAnnotationMetadata(Element annotatedElement, AnnotationMirror annotationMirror) {
        List<AnnotationExcerpt<ExtractWith>> all = findAll(ExtractWith.class, annotationMirror.getAnnotationType().asElement());
        AnnotationMetadata annotationMetadata = AnnotationMetadata.of(annotationMirror);
        ExtractionContext context = new ExtractionContext(annotatedElement, annotationMirror, annotationMetadata);

        for (AnnotationExcerpt<ExtractWith> metadataStrategyResult : all) {
            ExtractWith annotation = metadataStrategyResult.instance();
            Class<? extends AnnotationMetadataExtractor> extractorClass = extractClass(annotation, ExtractWith::value);
            AnnotationMetadataExtractor strategy = injector.get(extractorClass);
            AnnotationMetadata extract = strategy.extract(context, extractionEnvironment);
            if (extract != null) {
                return extract;
            }
        }
        return annotationMetadata;
    }

    public Optional<AnnotationMirror> findAnnotationMirrorInherited(Element element, Class<? extends Annotation> annotation) {
        for (AnnotationMirror am : element.getAnnotationMirrors()) {
            if (isJdkAnnotation(am)) {
                continue;
            }

            if (types.isSameType(am.getAnnotationType(), elements.getTypeElement(annotation.getCanonicalName()).asType())) {
                return Optional.of(am);
            }

            Optional<AnnotationMirror> metaAnnotated = findAnnotationMirrorInherited(am.getAnnotationType().asElement(), annotation);
            if (metaAnnotated.isPresent()) {
                return metaAnnotated;
            }
        }
        return Optional.empty();
    }

    private record ExtractionEnvironmentImplementation(Annotations annotations) implements ExtractionEnvironment {

        @Override
        public boolean isAnnotatedWith(Element element, Class<? extends Annotation> annotationClass) {
            return Annotations.search().by(annotationClass).isPresentIn(element);
        }

        @Override
        public boolean hasAnnotationByName(Element element, String annotationName) {
            return search().byName(annotationName).isPresentIn(element);
        }

        @Override
        public boolean hasAnnotationByName(Element element, Class<? extends Annotation> annotationName) {
            return search().byName(annotationName).isPresentIn(element);
        }

        @Override
        public boolean hasAnnotationByName(DeclaredType declaredType, Class<? extends Annotation> annotation) {
            return search().byName(annotation).isPresentIn(declaredType.asElement());
        }

        @Override
        public boolean hasAnnotationByName(DeclaredType declaredType, String annotationName) {
            return search().byName(annotationName).isPresentIn(declaredType.asElement());
        }

        @Override
        public <T extends Annotation> Optional<T> getAnnotation(AnnotationMirror annotationMirror, Class<T> annotation) {
            return search().by(annotation).findFirstIn(annotationMirror);
        }

        @Override
        public <T extends Annotation> Optional<T> getAnnotation(Element element, Class<T> annotation) {
            return search().by(annotation).findFirstIn(element);
        }

        @Override
        public <T extends Annotation> List<AnnotationExcerpt<T>> findAll(Class<T> annotationType, Element element) {
            return search().by(annotationType).findAllExcerptsIn(element);
        }
    }

    /**
     * Create a new annotation search builder.
     */
    public static SearchBuilder search() {
        return new SearchBuilder(types, elements);
    }

    public static <T extends Annotation> T proxy(AnnotationMirror mirror, Class<T> annotationType) {
        if (types == null || elements == null) {
            throw new IllegalStateException("Annotations must be initialized before proxying");
        }

        Map<String, Object> valuesByName = new HashMap<>();
        mirror.getElementValues().forEach((executableElement, annotationValue) -> {
            String name = executableElement.getSimpleName().toString();
            Object value = annotationValue.getValue();
            valuesByName.put(name, value);
        });

        logger.info(() -> "Extracted annotation values for " + mirror + ": " + valuesByName);

        return (T) java.lang.reflect.Proxy.newProxyInstance(
                annotationType.getClassLoader(),
                new Class<?>[]{annotationType},
                new AnnotationProxy<T>(elements, types, annotationType, valuesByName)
        );
    }
}
