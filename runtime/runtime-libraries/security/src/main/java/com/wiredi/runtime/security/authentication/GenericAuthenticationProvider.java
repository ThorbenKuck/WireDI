package com.wiredi.runtime.security.authentication;

import org.jetbrains.annotations.Nullable;

public interface GenericAuthenticationProvider<T> extends AuthenticationProvider {

    @Override
    default @Nullable Authentication extract(Object source) {
        if (!canExtractFrom(source)) {
            throw new IllegalArgumentException("The source is not of the correct type");
        }

        return extractFrom((T) source);
    }

    Authentication extractFrom(T source);

    Class<T> sourceClass();

    @Override
    default boolean canExtractFrom(Object source) {
        return sourceClass().isAssignableFrom(source.getClass());
    }
}
