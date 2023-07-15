package com.wiredi.compiler.domain.entities.methods.identifiableprovider;

import com.squareup.javapoet.*;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.TypeIdentifiers;
import com.wiredi.compiler.domain.entities.methods.MethodFactory;
import com.wiredi.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdditionalWireTypesMethod implements MethodFactory {

	private final List<TypeMirror> typeElements;
	private final TypeIdentifiers typeIdentifiers;
	private static final TypeName TYPE_IDENTIFIER_LIST = ParameterizedTypeName.get(
			ClassName.get(List.class),
			ParameterizedTypeName.get(
					ClassName.get(TypeIdentifier.class),
					WildcardTypeName.subtypeOf(Object.class)
			)
	);

	public AdditionalWireTypesMethod(List<TypeMirror> typeElements, TypeIdentifiers typeIdentifiers) {
		this.typeElements = typeElements;
		this.typeIdentifiers = typeIdentifiers;
	}

	@Override
	public void append(TypeSpec.Builder builder, AbstractClassEntity<?> entity) {
		List<CodeBlock> typeIdentifier = new ArrayList<>();

		typeElements.stream()
				.filter(Objects::nonNull)
				.filter(it -> it.getKind() != TypeKind.NONE)
				.map(typeIdentifiers::newTypeIdentifier)
				.forEach(typeIdentifier::add);

		if (typeIdentifier.isEmpty()) {
			return;
		}

		builder.addField(
				FieldSpec.builder(TYPE_IDENTIFIER_LIST, "ADDITIONAL_WIRE_TYPES")
						.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
						.initializer(
								CodeBlock.builder()
										.add("$T.of(\n", List.class)
										.indent()
										.add(CodeBlock.join(typeIdentifier, ",\n"))
										.unindent()
										.add("\n)")
										.build()
						)
						.build()
		).addMethod(
				MethodSpec.methodBuilder("additionalWireTypes")
						.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
						.returns(TYPE_IDENTIFIER_LIST)
						.addAnnotation(Override.class)
						.addAnnotation(NotNull.class)
						.addStatement("return ADDITIONAL_WIRE_TYPES")
						.build()
		);

	}
}
