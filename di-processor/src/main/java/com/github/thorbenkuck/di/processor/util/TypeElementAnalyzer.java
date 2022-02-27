package com.github.thorbenkuck.di.processor.util;

import com.github.thorbenkuck.di.processor.foundation.ProcessorContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TypeElementAnalyzer {

    @NotNull
    public static List<TypeElement> getSuperElements(@Nullable final TypeMirror mirror) {
        if (mirror == null) {
            return new ArrayList<>();
        }

        final Set<TypeElement> result = new HashSet<>();
        final List<? extends TypeMirror> mirrors = ProcessorContext.getTypes().directSupertypes(mirror);

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
