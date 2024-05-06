package com.wiredi.compiler.domain;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class AnnotationField<T extends AnnotationValue> {

    @Nullable
    private final String name;
    @Nullable
    private final AnnotationMirror parent;
    @Nullable
    private final T value;

    private AnnotationField(
            @Nullable AnnotationMirror parent,
            @Nullable String name,
            @Nullable T value
    ) {
        this.value = value;
        this.name = name;
        this.parent = parent;
    }

    public static <T extends AnnotationValue> AnnotationField<T> of(
            @NotNull AnnotationMirror parent,
            @NotNull String name,
            @NotNull T value
    ) {
        return new AnnotationField<>(parent, name, value);
    }

    public static <T extends AnnotationValue> AnnotationField<T> empty() {
        return new AnnotationField<>(null, null, null);
    }

    public boolean isPresent() {
        return value != null && parent != null && name != null;
    }

    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (value != null) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    @NotNull
    private Object getValue() {
        if (value == null) {
            throw new IllegalStateException();
        }

        return value.getValue();
    }

    @NotNull
    private AnnotationMirror getParent() {
        if (parent == null) {
            throw new IllegalStateException();
        }

        return parent;
    }

    @NotNull
    private String getName() {
        if (name == null) {
            throw new IllegalStateException();
        }

        return name;
    }

    public <S> Optional<S> map(Function<@NotNull AnnotationField<T>, @Nullable S> function) {
        if (value != null) {
            return Optional.of(this)
                    .map(function);
        }
        return Optional.empty();
    }

    public List<TypeMirror> asArrayOfClasses() {
        Object innerValue = getValue();
        if (List.class.isAssignableFrom(innerValue.getClass())) {
            List<AnnotationValue> values = (List<AnnotationValue>) innerValue;
            List<TypeMirror> result = new ArrayList<>();
            for (AnnotationValue next : values) {
                result.add((TypeMirror) next.getValue());
            }
            return result;
        }

        throw new IllegalStateException("The field " + name + " of " + parent + " is not an array of classes");
    }

    public TypeMirror asClass() {
        Object innerValue = getValue();
        if (TypeMirror.class.isAssignableFrom(innerValue.getClass())) {
            return (TypeMirror) innerValue;
        }

        throw new IllegalStateException("The field " + name + " of " + parent + " is not a class");
    }

    public List<String> asArrayOfStrings() {
        Object innerValue = getValue();
        if (List.class.isAssignableFrom(innerValue.getClass())) {
            List<AnnotationValue> values = (List<AnnotationValue>) innerValue;
            List<String> result = new ArrayList<>();
            for (AnnotationValue next : values) {
                result.add((String) next.getValue());
            }
            return result;
        }

        throw new IllegalStateException("The field " + name + " of " + parent + " is not an array of strings");
    }

    public String asStrings() {
        Object innerValue = getValue();
        if (String.class.isAssignableFrom(innerValue.getClass())) {
            return (String) innerValue;
        }

        throw new IllegalStateException("The field " + name + " of " + parent + " is not a string");
    }
}
