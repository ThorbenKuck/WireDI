package com.wiredi.runtime.domain.factories;

import com.wiredi.runtime.domain.provider.QualifiedTypeIdentifier;
import com.wiredi.runtime.domain.provider.TypeIdentifier;

public class MissingBeanException extends RuntimeException {

    private final TypeIdentifier<?> type;

    public MissingBeanException(TypeIdentifier<?> type, String message) {
        super(message);
        this.type = type;
    }

    public static MissingBeanException missingFactory(TypeIdentifier<?> type) {
        return new MissingBeanException(type, "No type factory found for type " + type + ". Are you sure you have wired any class of the type?");
    }

    public static MissingBeanException unableToCreate(TypeIdentifier<?> type) {
        return new MissingBeanException(type, "Unable to create bean of type " + type + ".");
    }

    public static MissingBeanException unableToCreate(QualifiedTypeIdentifier<?> type) {
        return new MissingBeanException(type.type(), "Unable to create bean of type " + type + ".");
    }

    public TypeIdentifier<?> type() {
        return type;
    }
}
