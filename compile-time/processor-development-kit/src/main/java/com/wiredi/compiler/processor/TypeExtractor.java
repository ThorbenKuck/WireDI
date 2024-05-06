package com.wiredi.compiler.processor;

import com.wiredi.annotations.Wire;
import com.wiredi.compiler.domain.Annotations;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.wiredi.compiler.processor.PropertyKeys.ADDITIONAL_WIRE_TYPES_SUPPORT_INHERITANCE;

public class TypeExtractor {

    private static final List<TypeKind> NON_SUPER_TYPES = List.of(TypeKind.NONE, TypeKind.NULL, TypeKind.VOID);
    private final Types types;
    private final Annotations annotations;
    private final ProcessorProperties properties;

    public TypeExtractor(Types types, Annotations annotations, ProcessorProperties properties) {
        this.types = types;
        this.annotations = annotations;
        this.properties = properties;
    }

    public List<TypeMirror> getAdditionalWireTypesOf(TypeElement typeElement) {
        List<TypeMirror> classArrayValueFromAnnotation = annotations.getClassArrayValueFromAnnotation(typeElement, Wire.class, "to");
        if (!classArrayValueFromAnnotation.isEmpty()) {
            return filterAdditionalWireTypes(classArrayValueFromAnnotation);
        }

        if (properties.isEnabled(ADDITIONAL_WIRE_TYPES_SUPPORT_INHERITANCE)) {
            return filterAdditionalWireTypes(getAllSuperTypes(typeElement));
        } else {
            return filterAdditionalWireTypes(getDeclaredSuperTypes(typeElement));
        }
    }

    public List<TypeMirror> getAllSuperTypes(TypeElement typeElement) {
        List<TypeMirror> superTypes = new ArrayList<>(getDeclaredSuperTypes(typeElement));

        var nestedTypes = superTypes.stream()
                .flatMap(it -> getAllSuperTypes((TypeElement) types.asElement(it)).stream())
                .filter(Objects::nonNull)
                .filter(it -> !NON_SUPER_TYPES.contains(it.getKind()))
                .toList();
        superTypes.addAll(nestedTypes);

        return superTypes;
    }

    public List<TypeMirror> getDeclaredSuperTypes(TypeElement typeElement) {
        if (typeElement == null) {
            return List.of();
        }

        List<TypeMirror> interfaces = new ArrayList<>(typeElement.getInterfaces());
        interfaces.add(typeElement.getSuperclass());
        return interfaces;
    }

    private List<TypeMirror> filterAdditionalWireTypes(List<TypeMirror> typeMirrors) {
        List<String> ignore = properties.getAll(PropertyKeys.ADDITIONAL_WIRE_TYPES_IGNORE);

        return typeMirrors.stream()
                .filter(it -> !ignore.contains(it.toString()))
                .collect(Collectors.toList());
    }
}
