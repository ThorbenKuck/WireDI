package com.wiredi.compiler.processor.business;

import com.wiredi.annotations.Initialize;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.domain.TypeUtils;
import com.wiredi.compiler.domain.injection.FieldInjectionPoint;
import com.wiredi.compiler.domain.injection.InjectionPoints;
import com.wiredi.compiler.domain.injection.MethodInjectionPoint;
import com.wiredi.compiler.domain.injection.PostConstructInjectionPoint;
import com.wiredi.compiler.domain.injection.constructor.ConstructorInjectionPoint;
import com.wiredi.compiler.logger.slf4j.CompileTimeLogger;
import com.wiredi.compiler.logger.slf4j.CompileTimeLoggerFactory;
import com.wiredi.compiler.processor.lang.utils.TypeElements;
import com.wiredi.runtime.domain.Eager;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InjectionPointService {

    private static final CompileTimeLogger logger = CompileTimeLoggerFactory.getLogger(InjectionPointService.class);
    private final TypeElements typeElements;

    public InjectionPointService(TypeElements typeElements) {
        this.typeElements = typeElements;
    }

    public InjectionPoints injectionPoints(TypeElement typeElement) {
        return new InjectionPoints(
                findFieldInjectionPoints(typeElement),
                findMethodInjectionPoints(typeElement),
                findConstructorInjectionPoint(typeElement),
                findPostConstructFunctions(typeElement)
        );
    }

    public List<FieldInjectionPoint> findFieldInjectionPoints(TypeElement typeElement) {
        return typeElements.fieldsOf(typeElement)
                .stream()
                .filter(it -> Annotations.search().byName("Inject").isPresentIn(it))
                .filter(it -> {
                    boolean isFinal = it.getModifiers().contains(Modifier.FINAL);
                    if (isFinal) {
                        logger.warn(it, () -> "This field is marked as an injection point, but final fields cannot be injected!");
                    }
                    return !isFinal;
                })
                .map(FieldInjectionPoint::new)
                .toList();
    }

    public List<MethodInjectionPoint> findMethodInjectionPoints(TypeElement typeElement) {
        return typeElements.methodsOf(typeElement)
                .stream()
                .filter(it -> Annotations.search().byName("Inject").isPresentIn(it))
                .map(MethodInjectionPoint::new)
                .toList();
    }

    @Nullable
    public ConstructorInjectionPoint findConstructorInjectionPoint(TypeElement typeElement) {
        return TypeUtils.findPrimaryConstructor(typeElement)
                .map(ConstructorInjectionPoint::resolve)
                .orElse(null);
    }

    @NotNull
    public List<? extends PostConstructInjectionPoint> findPostConstructFunctions(TypeElement typeElement) {
        List<PostConstructInjectionPoint> injectionPoints = new ArrayList<>();
        typeElements.methodsOf(typeElement)
                .stream()
                .map(it -> {
                    if (Annotations.search().byName("PostConstruct").isPresentIn(it)) {
                        return new PostConstructInjectionPoint(it, false);
                    }

                    return Annotations.search().byType(Initialize.class)
                            .findFirstIn(it)
                            .map(annotation -> new PostConstructInjectionPoint(it, annotation.async()))
                            .orElse(null);
                })
                .filter(Objects::nonNull)
                .forEach(injectionPoints::add);

        if (typeElements.iSOfType(typeElement.asType(), Eager.class)) {
            logger.debug(() -> "Detected " + typeElement + " as eager initialized type. Invoking setup method post construct.");
            typeElements.methodsOf(typeElement)
                    .stream()
                    .filter(method -> method.getSimpleName().toString().equals("setup"))
                    .filter(method -> method.getParameters().isEmpty())
                    .filter(method -> !method.getModifiers().contains(Modifier.ABSTRACT))
                    .filter(method -> !method.getModifiers().contains(Modifier.DEFAULT))
                    .findFirst()
                    .ifPresent(method -> {
                        // Füge die initialize() Methode als PostConstruct Injection Point hinzu
                        PostConstructInjectionPoint eagerInitPoint = new PostConstructInjectionPoint(method, false);
                        injectionPoints.add(eagerInitPoint);
                    });
        }

        return injectionPoints;
    }
}
