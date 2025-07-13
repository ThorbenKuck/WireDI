package com.wiredi.runtime.domain.provider;

import com.wiredi.runtime.qualifier.QualifierType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

public record QualifiedTypeIdentifier<T>(
        @NotNull TypeIdentifier<T> type,
        @NotNull QualifierType qualifier
) {
    public static <T> QualifiedTypeIdentifier<T> qualified(@NotNull TypeIdentifier<T> type, @NotNull QualifierType qualifierType) {
        return new QualifiedTypeIdentifier<>(type, qualifierType);
    }

    public static <T> QualifiedTypeIdentifier<T> qualified(@NotNull Class<T> type, @NotNull QualifierType qualifierType) {
        return new QualifiedTypeIdentifier<>(TypeIdentifier.of(type), qualifierType);
    }

    public static <T> QualifiedTypeIdentifier<T> qualified(@NotNull Type type, @NotNull QualifierType qualifierType) {
        return new QualifiedTypeIdentifier<>(TypeIdentifier.of(type), qualifierType);
    }
}
