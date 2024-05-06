package com.wiredi.runtime.exceptions;

import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;

public class MultiplePrimaryProvidersRegisteredException extends DiLoadingException {
    public MultiplePrimaryProvidersRegisteredException(TypeIdentifier<?> typeIdentifier, IdentifiableProvider<?> first, IdentifiableProvider<?> second) {
        super("Multiple primary providers registered for the type " + typeIdentifier + ": " + System.lineSeparator()
                + " - " + first + System.lineSeparator()
                + " - " + second);
    }
}
