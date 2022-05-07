package com.github.thorbenkuck.di.processor;

import com.github.thorbenkuck.di.annotations.PrimaryWireType;
import com.github.thorbenkuck.di.annotations.Wire;
import com.github.thorbenkuck.di.annotations.WirePriority;
import com.github.thorbenkuck.di.processor.exceptions.ProcessingException;
import com.github.thorbenkuck.di.processor.utils.TypeElementAnalyzer;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WireAnnotationInformationExtractor {

    public static WireInformation.Builder startExtractionFor(@NotNull TypeElement typeElement) {
        boolean singleton = isSingleton(typeElement);
        boolean proxyExpected = isProxyExpected(typeElement);
        TypeElement primaryWireType = getPrimaryWireType(typeElement);
        List<TypeElement> wiredToElements = extractAllWiredTyped(typeElement);

        PackageElement packageElement = ProcessorContext.mapWithElements(elements -> elements.getPackageOf(typeElement));

        return WireInformation.builderFor(typeElement)
                .asProxy(proxyExpected)
                .asSingleton(singleton)
                .withPrimaryWireType(primaryWireType)
                .addWiredToElements(wiredToElements)
                .withWirePriority(getWirePriority(typeElement).orElse(null))
                .atPackage(packageElement);
    }

    public static WireInformation extractOf(@NotNull TypeElement typeElement) {
        return startExtractionFor(typeElement)
                .withPrimaryConstructor(findBestSuitedInvokableConstructor(typeElement).orElse(null))
                .build();
    }

    public static WireInformation extractForProvider(@NotNull ExecutableElement method) {
        if(method.getReturnType().getKind() == TypeKind.VOID) {
            throw new ProcessingException(method, "Only methods with non-void return values may be annotated with @Provider");
        }

        TypeElement parentClass = (TypeElement) method.getEnclosingElement();
        TypeElement primaryWireType = (TypeElement) ProcessorContext.getTypes().asElement(method.getReturnType());
        WireInformation.Builder builder = startExtractionFor(parentClass);

        return startExtractionFor(parentClass)
                .buildByMethod(method)
                .withPrimaryWireType(primaryWireType)
                .setWiredToElement(primaryWireType)
                .asSingleton(true)
                .asProxy(false)
                .build();
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

    public static boolean isSingleton(@NotNull TypeElement typeElement) {
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
