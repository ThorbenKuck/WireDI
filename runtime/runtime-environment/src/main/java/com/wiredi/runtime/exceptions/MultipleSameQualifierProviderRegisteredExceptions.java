package com.wiredi.runtime.exceptions;

import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.qualifier.QualifierType;

public class MultipleSameQualifierProviderRegisteredExceptions extends DiLoadingException {
    public MultipleSameQualifierProviderRegisteredExceptions(QualifierType qualifierType, IdentifiableProvider<?> first, IdentifiableProvider<?> second) {
        super("Multiple providers where registered for the same qualifier type " + qualifierType + "." + System.lineSeparator()
                + " - " + first + System.lineSeparator()
                + " - " + second);
    }
}
