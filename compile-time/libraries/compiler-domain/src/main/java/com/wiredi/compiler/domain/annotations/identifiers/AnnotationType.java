package com.wiredi.compiler.domain.annotations.identifiers;

import com.wiredi.compiler.domain.annotations.AnnotationIdentifier;
import com.wiredi.logging.Logging;
import com.wiredi.runtime.values.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.util.Objects;

/**
 * Identifies annotations by their concrete type.
 * Supports inheritance if the annotation has @Inherited.
 */
public class AnnotationType<T extends Annotation> implements AnnotationIdentifier {

    private static final Logging logger = Logging.getInstance(AnnotationType.class);

    @NotNull
    private final Class<T> type;
    private final Value<Boolean> supportsInheritance;
    @Nullable
    private final Types types;
    @Nullable
    private final Elements elements;
    private TypeMirror targetType;

    public AnnotationType(@NotNull Class<T> type, @Nullable Types types, @Nullable Elements elements) {
        this.type = type;
        this.types = types;
        this.elements = elements;
        this.supportsInheritance = Value.lazy(() -> type.isAnnotationPresent(Inherited.class));
        if (elements != null) {
            targetType = elements.getTypeElement(type.getCanonicalName()).asType();
        }

        if (targetType == null) {
            logger.warn("TypeElement for " + type.getCanonicalName() + " not found");
        }

        if (types == null || elements == null) {
            logger.warn("Environment is not initialized");
        }
    }

    @Override
    public boolean matches(@NotNull AnnotationMirror annotationMirror) {
        DeclaredType targetAnnotationType = annotationMirror.getAnnotationType();
        if (types == null || elements == null || targetType == null) {
            String actualName = targetAnnotationType.toString();
            return actualName.equals(type.getName()) || actualName.equals(type.getCanonicalName());
        } else {
            logger.trace("Checking annotation type: " + targetAnnotationType + " vs " + targetType + ": ");
            return types.isSameType(types.erasure(targetAnnotationType), types.erasure(targetType));
        }
    }

    @Override
    public boolean supportsInheritance() {
        return supportsInheritance.get();
    }

    @NotNull
    public Class<T> getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnnotationType<?> that = (AnnotationType<?>) o;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public String toString() {
        return "AnnotationType{" + type.getName() + '}';
    }
}
