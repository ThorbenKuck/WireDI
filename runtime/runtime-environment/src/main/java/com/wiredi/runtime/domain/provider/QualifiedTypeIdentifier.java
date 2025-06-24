package com.wiredi.runtime.domain.provider;

import com.wiredi.runtime.qualifier.QualifierType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public record QualifiedTypeIdentifier<T>(
        @NotNull TypeIdentifier<T> type,
        @Nullable QualifierType qualifier
) {
    public static <T> QualifiedTypeIdentifier<T> unqualified(@NotNull TypeIdentifier<T> type) {
        return new QualifiedTypeIdentifier<>(type, null);
    }

    public static <T> QualifiedTypeIdentifier<T> unqualified(@NotNull Class<T> type) {
        return new QualifiedTypeIdentifier<>(TypeIdentifier.of(type), null);
    }

    public static <T> QualifiedTypeIdentifier<T> unqualified(@NotNull Type type) {
        return new QualifiedTypeIdentifier<>(TypeIdentifier.of(type), null);
    }

    public static <T> QualifiedTypeIdentifier<T> qualified(@NotNull TypeIdentifier<T> type, @NotNull QualifierType qualifierType) {
        return new QualifiedTypeIdentifier<>(type, qualifierType);
    }

    public static <T> QualifiedTypeIdentifier<T> qualified(@NotNull Class<T> type, @NotNull QualifierType qualifierType) {
        return new QualifiedTypeIdentifier<>(TypeIdentifier.of(type), qualifierType);
    }

    public static <T> QualifiedTypeIdentifier<T> qualified(@NotNull Type type, @NotNull QualifierType qualifierType) {
        return new QualifiedTypeIdentifier<>(TypeIdentifier.of(type), qualifierType);
    }

    public boolean isQualified() {
        return qualifier != null;
    }

    public QualifiedTypeIdentifier<T> qualify(@NotNull QualifierType qualifierType) {
        return new QualifiedTypeIdentifier<>(type, qualifierType);
    }
}
