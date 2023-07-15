package com.wiredi.compiler.domain.entities.methods.identifiableprovider;

import com.squareup.javapoet.*;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.TypeIdentifiers;
import com.wiredi.compiler.domain.entities.methods.MethodFactory;
import com.wiredi.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

public class TypeMethod implements MethodFactory {

	private final TypeIdentifiers typeIdentifiers;
	private final TypeMirror primaryWireType;

	public TypeMethod(TypeIdentifiers typeIdentifiers, TypeMirror primaryWireType) {
		this.typeIdentifiers = typeIdentifiers;
		this.primaryWireType = primaryWireType;
	}

	@Override
	public void append(TypeSpec.Builder builder, AbstractClassEntity<?> entity) {
		String variableName = "PRIMARY_WIRE_TYPE";
		ParameterizedTypeName typeName = ParameterizedTypeName.get(ClassName.get(TypeIdentifier.class), TypeName.get(primaryWireType));

		builder.addField(
				FieldSpec.builder(typeName, variableName)
						.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
						.initializer(typeIdentifiers.newTypeIdentifier(primaryWireType)).build()
		).addMethod(
				MethodSpec.methodBuilder("type")
						.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
						.addAnnotation(Override.class)
						.addAnnotation(NotNull.class)
						.returns(typeName)
						.addStatement("return $L", variableName)
						.build()
		);
	}
}
