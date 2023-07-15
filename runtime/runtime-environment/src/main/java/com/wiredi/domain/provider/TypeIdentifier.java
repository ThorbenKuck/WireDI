package com.wiredi.domain.provider;

import com.google.common.primitives.Primitives;
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

    public static TypeIdentifier<Object> OBJECT = new TypeIdentifier<>(Object.class);

    /**
     * Creates a new TypeIdentifier for the given type.
     *
     * This method behaves like {@link #of(Class)}, but
     *
     * @param type
     * @return
     * @param <T>
     */
    @NotNull
    public static <T> TypeIdentifier<? extends T> just(@NotNull Class<T> type) {
        return new TypeIdentifier<>(type);
    }

    @NotNull
    public static <T> TypeIdentifier<T> of(@NotNull Class<T> type) {
        if (type.isPrimitive()) {
            return new TypeIdentifier<>(Primitives.wrap(type));
        }
        return new TypeIdentifier<>(type);
    }

    @NotNull
    public static <T> TypeIdentifier<? extends T> resolve(@NotNull T instance) {
        return of((Class<T>) instance.getClass());
    }

    private TypeIdentifier(@NotNull Class<T> rootType) {
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
        if (genericTypes.isEmpty()) {
            return rootType.getName();
        }

        return rootType.getName() + "<" + genericTypes.stream().map(TypeIdentifier::toString).collect(Collectors.joining(", ")) +  ">";
    }
}
