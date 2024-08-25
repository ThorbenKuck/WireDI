package com.wiredi.compiler.domain;

import com.wiredi.compiler.logger.Logger;
import com.wiredi.runtime.domain.AnnotationMetaData;
import jakarta.inject.Inject;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.util.*;
import java.util.function.Function;

public class Annotations {

    private static final Logger logger = Logger.get(Annotations.class);
    @Inject
    private Elements elements;
    @Inject
    private Types types;

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
        return element.toString().startsWith("java.lang.annotation") || element.toString().startsWith("kotlin.annotation");
    }

    public static boolean isJdkAnnotation(AnnotationMirror annotationMirror) {
        return annotationMirror.getAnnotationType().asElement().toString().startsWith("java.lang.annotation")
                || annotationMirror.getAnnotationType().asElement().toString().startsWith("kotlin.");
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
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            if (isNotJdkAnnotation(annotationMirror)) {
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

    public static <T extends Annotation> List<Result<T>> findAll(Class<T> annotationType, Element element) {
        List<Result<T>> result = new ArrayList<>();
        boolean inheritable = annotationType.isAnnotationPresent(Inherited.class);

        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            if (isJdkAnnotation(annotationMirror)) {
                continue;
            }

            if (inheritable && isMetaAnnotatedWith(annotationMirror.getAnnotationType().asElement(), annotationType)) {
                T annotation = annotationMirror.getAnnotationType().asElement().getAnnotation(annotationType);
                result.add(new Result<>(annotation, AnnotationMetaData.of(annotationMirror)));
            }

            if (Objects.equals(annotationMirror.getAnnotationType().asElement().toString(), annotationType.getName())) {
                T annotation = element.getAnnotation(annotationType);
                result.add(new Result<>(annotation, AnnotationMetaData.of(annotationMirror)));
            }
        }

        return result;
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

    // ########## Non static fields ###########

    public Optional<AnnotationMetaData> findFirstAnnotationMirror(Class<? extends Annotation> annotation, ClassEntity classEntity) {
        Element element = types.asElement(classEntity.rootType());
        boolean inheritable = annotation.isAnnotationPresent(Inherited.class);

        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            if (isJdkAnnotation(annotationMirror)) {
                continue;
            }

            if (inheritable && isMetaAnnotatedWith(annotationMirror.getAnnotationType().asElement(), annotation)) {
                return Optional.of(AnnotationMetaData.of(annotationMirror));
            }

            if (Objects.equals(annotationMirror.getAnnotationType().asElement().toString(), annotation.getName())) {
                return Optional.ofNullable(AnnotationMetaData.of(annotationMirror));
            }
        }

        return Optional.empty();
    }

    public Optional<AnnotationMirror> findAnnotationMirror(Element element, Class<? extends Annotation> annotation) {
        for (AnnotationMirror am : element.getAnnotationMirrors()) {
            if (types.isSameType(am.getAnnotationType(), elements.getTypeElement(annotation.getCanonicalName()).asType())) {
                return Optional.of(am);
            }
        }
        return Optional.empty();
    }

    public AnnotationMirror getAnnotationMirror(Element element, Class<? extends Annotation> annotation) {
        return findAnnotationMirror(element, annotation)
                .orElseThrow(() -> new IllegalArgumentException("Could not find the annotation " + annotation + " on " + element));
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

    public Optional<TypeMirror> findClassFieldFromAnnotation(Element element, Class<? extends Annotation> annotation, String parameterName) {
        return findAnnotationField(element, annotation, parameterName)
                .map(AnnotationField::asClass);
    }

    public Optional<String> findStringFieldFromAnnotation(Element element, Class<? extends Annotation> annotation, String parameterName) {
        return findAnnotationField(element, annotation, parameterName)
                .map(AnnotationField::asStrings);
    }

    public record Result<T extends Annotation>(
            T annotation,
            AnnotationMetaData annotationMetaData
    ) {
    }
}
