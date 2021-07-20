package com.github.thorbenkuck.di.processor.util;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TypeElementAnalyzer {

    private final Types typeUtils;

    public TypeElementAnalyzer(Types typeUtils) {
        this.typeUtils = typeUtils;
    }

    public Collection<TypeElement> getSuperElements(TypeMirror mirror) {
        List<TypeElement> result = new ArrayList<>();
        if (mirror == null ) {
            return result;
        }

        List<? extends TypeMirror> mirrors = typeUtils.directSupertypes(mirror);

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
