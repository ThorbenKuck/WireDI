package com.wiredi.runtime.exceptions;

import com.wiredi.domain.provider.IdentifiableProvider;
import com.wiredi.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;

public class MultiplePrimaryProvidersRegisteredException extends DiLoadingException {
    public MultiplePrimaryProvidersRegisteredException(TypeIdentifier<?> typeIdentifier, IdentifiableProvider<?> first, IdentifiableProvider<?> second) {
        super("Multiple primary providers registered for the type " + typeIdentifier + ": " + System.lineSeparator()
                + " - " + first + System.lineSeparator()
                + " - " + second);
    }
}
