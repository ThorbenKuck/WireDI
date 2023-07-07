package com.wiredi.domain.provider;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TypeIdentifier<T> {

    @NotNull
    private final Class<T> rootType;

    @NotNull
    private final List<TypeIdentifier<?>> genericTypes = new ArrayList<>();

    @NotNull
    public static <T> TypeIdentifier<T> of(@NotNull Class<T> type) {
        return new TypeIdentifier<>(type);
    }

    @NotNull
    public static <T> TypeIdentifier<T> resolve(@NotNull T instance) {
        return of((Class<T>) instance.getClass());
    }

    public TypeIdentifier(@NotNull Class<T> rootType) {
        this.rootType = rootType;
    }

    @NotNull
    public <S extends T> TypeIdentifier<S> withGeneric(@NotNull Class<?> type) {
        return withGeneric(TypeIdentifier.of(type));
    }

    @NotNull
    public <S extends T> TypeIdentifier<S> withGeneric(@NotNull TypeIdentifier<?> type) {
        genericTypes.add(type);

        return (TypeIdentifier<S>) this;
    }

    public boolean isAssignableFrom(Class<?> type) {
        return rootType.isAssignableFrom(type);
    }

    @NotNull
    public Class<T> getRootType() {
        return rootType;
    }

    @NotNull
    public List<TypeIdentifier<?>> getGenericTypes() {
        return genericTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeIdentifier<?> that = (TypeIdentifier<?>) o;

        if (!Objects.equals(rootType, that.rootType)) {
            return false;
        }

        if (genericTypes.size() > that.genericTypes.size()) {
            return false;
        }

        for (int index = 0; index < genericTypes.size(); index++) {
            TypeIdentifier<?> current = genericTypes.get(index);
            TypeIdentifier<?> other = that.genericTypes.get(index);
            if (!current.equals(other)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootType, genericTypes);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(rootType.getName());
        if (!genericTypes.isEmpty()) {
            List<String> names = genericTypes.stream().map(TypeIdentifier::toString).toList();
            stringBuilder.append("<").append(String.join(", ", names)).append(">");
        }
        return stringBuilder.toString();
    }
}
