package com.wiredi.runtime.domain.provider;

import com.wiredi.runtime.qualifier.QualifierType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

/**
 * A type identifier wrapper, supporting qualifiers.
 * <p>
 * This record is used wherever a type identifier is required, but also can have a qualifier.
 * If {@link #qualifier()} returns null, the {@link #type()} is expected to be used unqualified.
 *
 * @param type      the type identifier to use
 * @param qualifier the (optional) qualifier to use
 * @param <T>       the generic type of the identifier
 */
public record QualifiedTypeIdentifier<T>(
        @NotNull TypeIdentifier<T> type,
        @Nullable QualifierType qualifier
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
