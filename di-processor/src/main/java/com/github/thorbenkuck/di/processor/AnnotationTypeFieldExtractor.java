package com.github.thorbenkuck.di.processor;

import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.function.Supplier;

public class AnnotationTypeFieldExtractor {

    public static <T> TypeMirror extractFromClassField(Supplier<Class<T>> supplier) {
        return extractFirstFromException(supplier);
    }

    public static TypeMirror extractFirstFromClassFields(Supplier<Class<?>[]> supplier) {
        return extractFirstFromException(supplier);
    }

    public static List<? extends TypeMirror> extractAllFromClassFields(Supplier<Class<?>[]> supplier) {
        try {
            supplier.get();
            throw new IllegalArgumentException("Invalid Supplier function provided!");
        } catch (javax.lang.model.type.MirroredTypesException e) {
            return e.getTypeMirrors();
        }
    }

    private static TypeMirror extractFirstFromException(Supplier<?> supplier) {
        try {
            supplier.get();
            throw new IllegalArgumentException("Invalid Supplier function provided!");
        } catch (javax.lang.model.type.MirroredTypesException e) {
            List<? extends TypeMirror> typeMirrors = e.getTypeMirrors();
            if (typeMirrors.isEmpty()) {
                throw new IllegalArgumentException("The provided supplier appears to not have return the correct class");
            }

            return typeMirrors.get(0);
        }
    }
}
