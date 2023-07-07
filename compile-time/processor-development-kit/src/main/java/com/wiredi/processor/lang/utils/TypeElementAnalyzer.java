package com.wiredi.processor.lang.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.*;

public class TypeElementAnalyzer {

    private final Types types;

    public TypeElementAnalyzer(Types types) {
        this.types = types;
    }

    @NotNull
    public TypeElement getEnclosingClass(@NotNull final ExecutableElement executableElement) {
        Element enclosingElement = executableElement.getEnclosingElement();

        while(enclosingElement != null && !enclosingElement.getKind().isClass()) {
            enclosingElement = executableElement.getEnclosingElement();
        }

        return Optional.ofNullable(enclosingElement)
                .map(TypeElement.class::cast)
                .orElseThrow(() -> new IllegalStateException("Could not determine encapsulating class"));
    }

    @NotNull
    public List<TypeElement> getSuperElements(@Nullable final TypeMirror mirror) {
        if (mirror == null) {
            return new ArrayList<>();
        }

        final Set<TypeElement> result = new HashSet<>();
        final List<? extends TypeMirror> mirrors = types.directSupertypes(mirror);

        if (mirrors == null || mirrors.isEmpty()) {
            return new ArrayList<>(result);
        }

        for (TypeMirror it : mirrors) {
            if (it.getKind() == TypeKind.DECLARED) {
                final TypeElement element = (TypeElement) ((DeclaredType) it).asElement();
                result.add(element);
            }
        }

        new ArrayList<>(result).forEach(it -> {
            final List<TypeElement> superElements = getSuperElements(it.asType());
            result.addAll(superElements);
            superElements.clear();
        });

        return new ArrayList<>(result);
    }
}
