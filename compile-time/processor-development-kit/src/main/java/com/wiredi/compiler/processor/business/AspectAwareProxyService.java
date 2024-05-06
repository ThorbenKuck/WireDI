package com.wiredi.compiler.processor.business;

import com.wiredi.annotations.aspects.AspectTarget;
import com.wiredi.annotations.aspects.Pure;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.domain.values.ProxyMethod;
import com.wiredi.compiler.processor.AspectIgnoredAnnotations;
import com.wiredi.compiler.processor.ProcessorProperties;
import com.wiredi.compiler.processor.PropertyKeys;

import javax.lang.model.element.*;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wiredi.compiler.processor.PropertyKeys.AOP_REQUIRES_ASPECT_TARGET_ANNOTATION;

public class AspectAwareProxyService {

    private final AspectIgnoredAnnotations aspectIgnoredAnnotations;
    private final ProcessorProperties properties;

    public AspectAwareProxyService(AspectIgnoredAnnotations aspectIgnoredAnnotations, ProcessorProperties properties) {
        this.aspectIgnoredAnnotations = aspectIgnoredAnnotations;
        this.properties = properties;
    }

    public List<ProxyMethod> findEligibleMethods(TypeElement typeElement) {
        if (Annotations.isAnnotatedWith(typeElement, Pure.class)) {
            return Collections.emptyList();
        }

        Stream<ExecutableElement> proxyMethodStream = typeElement.getEnclosedElements()
                .stream()
                .filter(it -> it.getKind() == ElementKind.METHOD)
                .filter(it -> !Annotations.isAnnotatedWith(it, Pure.class))
                .map(it -> (ExecutableElement) it)
                .filter(it -> !it.getModifiers().contains(Modifier.PRIVATE))
                .filter(it -> !it.getModifiers().contains(Modifier.FINAL))
                // This process can later work, but needs additional work
                .filter(it -> !it.getModifiers().contains(Modifier.ABSTRACT));

        if (properties.isEnabled(AOP_REQUIRES_ASPECT_TARGET_ANNOTATION)) {
                return proxyMethodStream.map(it -> new ProxyMethod(it, getAopEnabledAnnotations(it)))
                    .filter(it -> !it.proxyAnnotations().isEmpty())
                        .toList();
        } else {
            return proxyMethodStream.map(it -> new ProxyMethod(it, getAopEnabledAnnotations(it)))
                    .filter(it -> !it.proxyAnnotations().isEmpty())
                    .toList();
        }
    }

    public Set<AnnotationMirror> getAopEnabledAnnotations(ExecutableElement method) {
        return Stream.concat(
                method.getAnnotationMirrors()
                        .stream()
                        .filter(this::isUsedForAop),
                method.getParameters()
                        .stream()
                        .flatMap(it -> it.getAnnotationMirrors().stream())
                        .filter(this::isUsedForAop)
        ).collect(Collectors.toSet());
    }

    public boolean isUsedForAop(AnnotationMirror annotationMirror) {
        Element annotationElement = annotationMirror.getAnnotationType().asElement();
        if (aspectIgnoredAnnotations.isIgnored(annotationElement)) {
            return false;
        }

        if (properties.isEnabled(PropertyKeys.AOP_STRICT_ANNOTATION_TARGET)) {
            if(annotationElement.getAnnotation(AspectTarget.class) != null) {
                return true;
            }
        }

        return true;
    }
}
