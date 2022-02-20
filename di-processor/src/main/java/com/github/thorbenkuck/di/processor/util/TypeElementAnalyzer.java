package com.github.thorbenkuck.di.processor.util;

import com.github.thorbenkuck.di.processor.foundation.ProcessorContext;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

public class TypeElementAnalyzer {

    public static List<TypeElement> getSuperElements(TypeMirror mirror) {
        List<TypeElement> result = new ArrayList<>();
        if (mirror == null) {
            return result;
        }

        List<? extends TypeMirror> mirrors = ProcessorContext.getTypes().directSupertypes(mirror);

        if (mirrors == null || mirrors.isEmpty()) {
            return result;
        }

        for (TypeMirror it : mirrors) {
            if (it.getKind() == TypeKind.DECLARED) {
                TypeElement element = (TypeElement) ((DeclaredType) it).asElement();
                result.add(element);
            }
        }

        return result;
    }
}
