package com.wiredi.compiler.domain;

import com.wiredi.compiler.Injector;

import com.wiredi.compiler.errors.ProcessingException;
import com.wiredi.runtime.domain.annotations.*;
import jakarta.inject.Inject;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Annotations {

    private static final Logger logger = LoggerFactory.getLogger(Annotations.class);
    @Inject
    private Elements elements;
    @Inject
    private Types types;
    @Inject
    private Injector injector;
    private final ExtractionEnvironment extractionEnvironment = new ExtractionEnvironmentImplementation(this);

    public static boolean isNotJdkAnnotation(Element element) {
        return !isJdkAnnotation(element);
    }

    public static boolean isNotJdkAnnotation(AnnotationMirror annotationMirror) {
        return !isJdkAnnotation(annotationMirror);
    }

    public static boolean isJdkAnnotation(Element element) {
        if (element.getKind() != ElementKind.ANNOTATION_TYPE) {
            return false;
        }
        return element.toString().startsWith("java.lang.instance") || element.toString().startsWith("kotlin.instance");
    }

    public static boolean isJdkAnnotation(AnnotationMirror annotationMirror) {
        return annotationMirror.getAnnotationType().asElement().toString().startsWith("java.lang.annotation")
                || annotationMirror.getAnnotationType().asElement().toString().startsWith("kotlin.");
    }

    public static <T extends Annotation, S extends Class<?>> S extractClass(T annotation, Function<T, S> function) {
        try {
            return function.apply(annotation);
        } catch (MirroredTypeException e) {
            String qualifiedNameFromTypeMirror = getQualifiedNameFromTypeMirror(e.getTypeMirror());
            if (qualifiedNameFromTypeMirror == null) {
                logger.error("Could not extract class from {}", e.getTypeMirror());
                throw new IllegalStateException("Could not extract class from " + e.getTypeMirror());
            }
            try {
                return (S) Class.forName(qualifiedNameFromTypeMirror);
            } catch (ClassNotFoundException ex) {
                logger.info("Could not find class {}", qualifiedNameFromTypeMirror);
                throw new IllegalStateException("Could not find class " + qualifiedNameFromTypeMirror, ex);
            }
        }
    }

    public static <T extends Annotation> TypeMirror extractType(T annotation, Function<T, Class<?>> function) {
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

    public static boolean isAnnotatedWith(AnnotationMirror annotationMirror, Class<? extends Annotation> annotation) {
        return getAnnotation(annotationMirror, annotation).isPresent();
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

    public static <A extends Annotation> List<AnnotatedElement<A, ExecutableElement>> findAllAnnotatedMethods(TypeElement typeElement, Class<A> annotationType) {
        return typeElement.getEnclosedElements()
                .stream()
                .filter(it -> it.getKind() == ElementKind.METHOD && isAnnotatedWith(it, annotationType))
                .map(method -> getAnnotation(method, annotationType).map(annotation -> new AnnotatedElement<>(annotation, (ExecutableElement) method)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public static <A extends Annotation> List<AnnotatedElement<A, VariableElement>> findAllAnnotatedParameters(ExecutableElement typeElement, Class<A> annotationType) {
        return typeElement.getParameters()
                .stream()
                .filter(it -> it.getKind() == ElementKind.PARAMETER && isAnnotatedWith(it, annotationType))
                .map(method -> getAnnotation(method, annotationType).map(annotation -> new AnnotatedElement<>(annotation, (VariableElement) method)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
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

    public Optional<AnnotationMetadata> findFirstAnnotationMirror(Class<? extends Annotation> annotation, ClassEntity classEntity) {
        Element element = types.asElement(classEntity.rootType());
        boolean inheritable = annotation.isAnnotationPresent(Inherited.class);

        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            if (isJdkAnnotation(annotationMirror)) {
                continue;
            }

            if (inheritable && isMetaAnnotatedWith(annotationMirror.getAnnotationType().asElement(), annotation)) {
                return Optional.of(AnnotationMetadata.of(annotationMirror));
            }

            if (Objects.equals(annotationMirror.getAnnotationType().asElement().toString(), annotation.getName())) {
                return Optional.ofNullable(AnnotationMetadata.of(annotationMirror));
            }
        }

        return Optional.empty();
    }

    public Optional<AnnotationMirror> findAnnotationMirror(Element element, Class<? extends Annotation> annotation) {
        boolean inheritable = annotation.isAnnotationPresent(Inherited.class);

        for (AnnotationMirror am : element.getAnnotationMirrors()) {
            if (types.isSameType(am.getAnnotationType(), elements.getTypeElement(annotation.getCanonicalName()).asType())) {
                return Optional.of(am);
            }
        }

        if (inheritable) {
            return findAnnotationMirrorInherited(element, annotation);
        }

        return Optional.empty();
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

    public AnnotationMirror getAnnotationMirror(AnnotationMirror annotationMirror, Class<? extends Annotation> annotation) {
        return findAnnotationMirror(annotationMirror.getAnnotationType().asElement(), annotation)
                .orElseThrow(() -> new IllegalArgumentException("Could not find the instance " + annotation + " on " + annotationMirror));
    }

    public AnnotationMirror getAnnotationMirror(Element element, Class<? extends Annotation> annotation) {
        return findAnnotationMirror(element, annotation)
                .orElseThrow(() -> new IllegalArgumentException("Could not find the instance " + annotation + " on " + element));
    }

    public <T extends AnnotationValue> AnnotationField<T> findAnnotationField(AnnotationMirror annotationMirror, String name) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
            if (name.equals(entry.getKey().getSimpleName().toString())) {
                return AnnotationField.of(annotationMirror, name, (T) entry.getValue());
            }
        }
        return AnnotationField.empty();
    }

    public <T extends AnnotationValue> AnnotationField<T> findAnnotationField(Element element, Class<? extends Annotation> annotation, String name) {
        return findAnnotationMirror(element, annotation)
                .map(mirror -> this.<T>findAnnotationField(mirror, name))
                .orElse(AnnotationField.empty());
    }

    public List<TypeMirror> getClassArrayValueFromAnnotation(Element element, Class<? extends Annotation> annotation, String parameterName) {
        return findAnnotationField(element, annotation, parameterName)
                .map(AnnotationField::asArrayOfClasses)
                .orElse(Collections.emptyList());
    }

    public TypeMirror getClassValueFromAnnotation(Element element, Class<? extends Annotation> annotation, String parameterName) {
        return findAnnotationField(element, annotation, parameterName)
                .map(AnnotationField::asClass)
                .orElse(null);
    }

    public Optional<TypeMirror> findClassFieldFromAnnotation(Element element, Class<? extends Annotation> annotation, String parameterName) {
        return findAnnotationField(element, annotation, parameterName)
                .map(AnnotationField::asClass);
    }

    public Optional<String> findStringFieldFromAnnotation(Element element, Class<? extends Annotation> annotation, String parameterName) {
        return findAnnotationField(element, annotation, parameterName)
                .map(AnnotationField::asStrings);
    }

    private record ExtractionEnvironmentImplementation(Annotations annotations) implements ExtractionEnvironment {

        @Override
            public boolean isAnnotatedWith(Element element, Class<? extends Annotation> annotationClass) {
                return Annotations.isAnnotatedWith(element, annotationClass);
            }

            @Override
            public boolean hasAnnotationByName(Element element, String annotationName) {
                return hasByName(element, annotationName);
            }

            @Override
            public boolean hasAnnotationByName(Element element, Class<? extends Annotation> annotationName) {
                return hasByName(element, annotationName);
            }

            @Override
            public boolean hasAnnotationByName(DeclaredType declaredType, Class<? extends Annotation> annotation) {
                return Annotations.hasByName(declaredType, annotation.getSimpleName());
            }

            @Override
            public boolean hasAnnotationByName(DeclaredType declaredType, String annotationName) {
                return Annotations.hasByName(declaredType, annotationName);
            }

            @Override
            public <T extends Annotation> Optional<T> getAnnotation(AnnotationMirror annotationMirror, Class<T> annotation) {
                return Annotations.getAnnotation(annotationMirror, annotation);
            }

            @Override
            public <T extends Annotation> Optional<T> getAnnotation(Element element, Class<T> annotation) {
                return Annotations.getAnnotation(element, annotation);
            }

            @Override
            public <T extends Annotation> List<AnnotationExcerpt<T>> findAll(Class<T> annotationType, Element element) {
                return annotations.findAll(annotationType, element);
            }

            @Override
            public AnnotationMetadata getAnnotationMetadata(Element annotatedElement, AnnotationMirror annotationMirror) {
                return annotations.getAnnotationMetadata(annotatedElement, annotationMirror);
            }
        }
}
