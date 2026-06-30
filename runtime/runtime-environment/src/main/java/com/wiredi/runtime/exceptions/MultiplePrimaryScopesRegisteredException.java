package com.wiredi.runtime.exceptions;

import com.wiredi.runtime.domain.Scope;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;

public class MultiplePrimaryScopesRegisteredException extends DiLoadingException {
    public MultiplePrimaryScopesRegisteredException(TypeIdentifier<?> typeIdentifier, Scope first, Scope second) {
        super("Multiple primary providers registered for the type " + typeIdentifier + ": " + System.lineSeparator()
                + " - " + first + System.lineSeparator()
                + " - " + second);
    }
}
