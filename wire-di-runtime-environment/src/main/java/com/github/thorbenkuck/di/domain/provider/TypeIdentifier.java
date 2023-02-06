package com.github.thorbenkuck.di.domain.provider;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TypeIdentifier<T> {

    @NotNull
    private final Class<T> rootType;

    @NotNull
    private final List<Class<?>> genericTypes = new ArrayList<>();

    @NotNull
    public static <T> TypeIdentifier<T> of(@NotNull Class<T> type) {
        return new TypeIdentifier<>(type);
    }

    public TypeIdentifier(@NotNull Class<T> rootType) {
        this.rootType = rootType;
    }

    public TypeIdentifier(@NotNull Class<T> rootType, @NotNull List<Class<?>> genericTypes) {
        this.rootType = rootType;
        this.genericTypes.addAll(genericTypes);
    }

    @NotNull
    public TypeIdentifier<T> withGeneric(@NotNull Class<?> type) {
        genericTypes.add(type);

        return this;
    }

    @NotNull
    public Class<T> getRootType() {
        return rootType;
    }

    @NotNull
    public List<Class<?>> getGenericTypes() {
        return genericTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeIdentifier<?> that = (TypeIdentifier<?>) o;
        return Objects.equals(rootType, that.rootType) && Objects.equals(genericTypes, that.genericTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootType, genericTypes);
    }

    @Override
    public String toString() {
        return "GenericIdentifier{" +
                "rootType=" + rootType +
                ", genericTypes=" + genericTypes +
                '}';
    }
}
