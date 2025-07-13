package com.wiredi.compiler.domain;

import com.wiredi.compiler.errors.ProcessingException;
import jakarta.inject.Inject;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import java.util.List;
import java.util.Optional;

public class TypeUtils {

    public static PackageElement packageOf(Element element) {
        Element current = element;

        while (current.getKind() != ElementKind.PACKAGE) {
            current = element.getEnclosingElement();
            if (current.asType().getKind() == TypeKind.NONE) {
                throw new ProcessingException(element, "Could not extract the package of the element " + element);
            }
        }

        return (PackageElement) current;
    }

    public static Optional<ExecutableElement> findPrimaryConstructor(TypeElement typeElement) {
        List<? extends ExecutableElement> constructors = typeElement.getEnclosedElements().stream()
                .filter(it -> it.getKind() == ElementKind.CONSTRUCTOR)
                .map(it -> (ExecutableElement) it)
                .toList();

        if (constructors.isEmpty()) {
            return Optional.empty();
        } else if (constructors.size() == 1) {
            return Optional.ofNullable(constructors.get(0));
        }

        List<? extends ExecutableElement> suitableConstructors = constructors.stream()
                .filter(it -> Annotations.hasByName(it, Inject.class))
                .toList();

        if (suitableConstructors.isEmpty()) {
            return Optional.empty();
        } else if (suitableConstructors.size() == 1) {
            return Optional.ofNullable(constructors.get(0));
        } else {
            throw new ProcessingException(typeElement, "Could not determine a suitable constructor. Please make sure to have exactly one constructor annotated with @Inject");
        }
    }
}
