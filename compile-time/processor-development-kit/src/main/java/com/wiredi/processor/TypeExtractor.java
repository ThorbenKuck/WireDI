package com.wiredi.processor;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TypeExtractor {

	private final Types types;

	public TypeExtractor(Types types) {
		this.types = types;
	}

	private static final List<TypeKind> NON_SUPER_TYPES = List.of(TypeKind.NONE, TypeKind.NULL, TypeKind.VOID);

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
}
