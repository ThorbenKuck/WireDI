package com.wiredi.runtime.exceptions;

import com.wiredi.domain.provider.IdentifiableProvider;
import com.wiredi.qualifier.QualifierType;
import org.jetbrains.annotations.NotNull;

public class MultipleSameQualifierProviderRegisteredExceptions extends DiLoadingException {
    public MultipleSameQualifierProviderRegisteredExceptions(QualifierType qualifierType, IdentifiableProvider<?> first, IdentifiableProvider<?> second) {
        super("Multiple providers where registered for the same qualifier type " + qualifierType + "." + System.lineSeparator()
                + " - " + first + System.lineSeparator()
                + " - " + second);
    }
}
