package com.github.thorbenkuck.di.processor;

import com.github.thorbenkuck.di.annotations.PrimaryWireType;
import com.github.thorbenkuck.di.annotations.Wire;
import com.github.thorbenkuck.di.annotations.WirePriority;
import com.github.thorbenkuck.di.processor.foundation.ProcessingException;
import com.github.thorbenkuck.di.processor.foundation.ProcessorContext;
import com.github.thorbenkuck.di.processor.util.TypeElementAnalyzer;
import com.squareup.javapoet.ClassName;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class WireInformation {

    private final TypeElement wireCandidate;
    private final PackageElement targetPackage;
    private final Optional<ExecutableElement> primaryConstructor;
    private final Optional<Integer> wirePriority;
    private final boolean singleton;
    private final TypeElement primaryWireType;
    private boolean forceSingleton = false;
    private final boolean proxyExpected;
    private final List<TypeElement> wiredToElements;

    private WireInformation(
            TypeElement wireCandidate,
            boolean singleton,
            TypeElement primaryWireType,
            boolean proxyExpected,
            List<TypeElement> wiredToElements
    ) {
        this.wireCandidate = Objects.requireNonNull(wireCandidate, "The wire candidate is required");
        this.targetPackage = Objects.requireNonNull(ProcessorContext.getElements().getPackageOf(wireCandidate), "The target package could not be found");
        this.primaryConstructor = Objects.requireNonNull(findBestSuitedInvokableConstructor(wireCandidate), "A primary Constructor is required");
        this.wirePriority = getWirePriority(wireCandidate);
        this.singleton = singleton;
        this.primaryWireType = primaryWireType;
        this.proxyExpected = proxyExpected;
        this.wiredToElements = wiredToElements;
    }

    public <T extends Annotation> Optional<T> findAnnotation(Class<T> annotationType) {
        Optional<T> primaryOptional = Optional.ofNullable(
                primaryWireType.getAnnotation(annotationType)
        );

        if(primaryOptional.isPresent()) {
            return primaryOptional;
        }

        return Optional.ofNullable(
                wireCandidate.getAnnotation(annotationType)
        );
    }

    public String simpleClassName() {
        return primaryWireType.getSimpleName().toString();
    }

    public TypeElement getWireCandidate() {
        return primaryWireType;
    }

    public Optional<ExecutableElement> getPrimaryConstructor() {
        return primaryConstructor;
    }

    public Optional<Integer> getWirePriority() {
        return wirePriority;
    }

    public boolean isSingleton() {
        return forceSingleton || singleton;
    }

    public TypeElement getPrimaryWireType() {
        return primaryWireType;
    }

    public void forceSingleton() {
        this.forceSingleton = true;
    }

    public PackageElement getTargetPackage() {
        return targetPackage;
    }

    public List<TypeElement> getAllWireCandidates() {
        return wiredToElements;
    }

    public ClassName primaryClassName() {
        return ClassName.get(primaryWireType);
    }

    public ClassName realClassName() {
        return ClassName.get(wireCandidate);
    }

    public boolean isProxyExpected() {
        return proxyExpected;
    }

    public static WireInformation extractOf(TypeElement typeElement) {
        boolean singleton = isSingleton(typeElement);
        boolean proxyExpected = isProxyExpected(typeElement);
        TypeElement primaryWireType = getPrimaryWireType(typeElement);
        List<TypeElement> wiredToElements = extractAllWiredTyped(typeElement);

        return new WireInformation(typeElement, singleton, primaryWireType, proxyExpected, wiredToElements);
    }

    public static boolean isProxyExpected(TypeElement typeElement) {
        return Optional.ofNullable(typeElement.getAnnotation(Wire.class))
                .map(Wire::proxy)
                .orElse(false);
    }

    public static Optional<Integer> getWirePriority(TypeElement typeElement) {
        WirePriority annotation = typeElement.getAnnotation(WirePriority.class);

        return Optional.ofNullable(annotation).map(WirePriority::value);
    }

    public static boolean isSingleton(TypeElement typeElement) {
        Wire wireAnnotation = typeElement.getAnnotation(Wire.class);
        if (wireAnnotation != null && wireAnnotation.singleton()) {
            return true;
        }

        return typeElement.getAnnotation(Singleton.class) != null;
    }

    public static TypeElement getPrimaryWireType(TypeElement typeElement) {
        PrimaryWireType primaryWireType = typeElement.getAnnotation(PrimaryWireType.class);
        if (primaryWireType == null) {
            return typeElement;
        }

        TypeMirror expectedType = AnnotationTypeFieldExtractor.extractFromClassField(primaryWireType::value);
        Element element = ProcessorContext.getTypes().asElement(expectedType);
        if (!(element instanceof TypeElement)) {
            throw new IllegalStateException("The element " + element + " is not a TypeElement");
        }
        return (TypeElement) element;
    }

    public static Optional<ExecutableElement> findBestSuitedInvokableConstructor(TypeElement typeElement) {
        List<? extends ExecutableElement> constructors = typeElement.getEnclosedElements()
                .stream()
                .filter(it -> it.getKind() == ElementKind.CONSTRUCTOR)
                .filter(it -> !it.getModifiers().contains(Modifier.FINAL))
                .map(it -> (ExecutableElement) it)
                .collect(Collectors.toList());

        if (constructors.isEmpty()) {
            return Optional.empty();
        }
        if (constructors.size() == 1) {
            return Optional.of(constructors.get(0));
        }
        List<ExecutableElement> annotatedConstructor = FetchAnnotated.constructors(typeElement, Inject.class);
        if (annotatedConstructor.size() != 1) {
            throw new ProcessingException(typeElement, "You have to provide either one Constructor or annotated the constructor to use with javax.inject.@Inject");
        }

        return Optional.of(annotatedConstructor.get(0));
    }

    public static List<TypeElement> extractAllWiredTyped(TypeElement typeElement) {
        Wire annotation = typeElement.getAnnotation(Wire.class);

        if (annotation == null) {
            return Collections.singletonList(typeElement);
        } else {
            // TODO: Outsource into their own methods
            List<? extends TypeMirror> typeMirrors = AnnotationTypeFieldExtractor.extractAllFromClassFields(annotation::to);

            if (typeMirrors.isEmpty()) {
                List<TypeElement> superElements = TypeElementAnalyzer.getSuperElements(typeElement.asType());
                superElements.add(typeElement);
                return superElements;
            } else {
                Types types = ProcessorContext.getTypes();

                for (TypeMirror typeMirror : typeMirrors) {
                    if (!types.isAssignable(typeElement.asType(), typeMirror)) {
                        throw new ProcessingException(typeElement, "The annotated element " + typeElement + " is not assignable to " + typeMirror);
                    }
                }
                return typeMirrors.stream()
                        .map(types::asElement)
                        .map(it -> (TypeElement) it)
                        .collect(Collectors.toList());
            }
        }
    }
}
