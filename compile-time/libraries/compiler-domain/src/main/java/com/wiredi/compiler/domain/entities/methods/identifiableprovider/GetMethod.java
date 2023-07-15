package com.wiredi.compiler.domain.entities.methods.identifiableprovider;

import com.squareup.javapoet.*;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.entities.methods.MethodFactory;
import com.wiredi.lang.SafeReference;
import com.wiredi.runtime.WireRepository;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

public class GetMethod implements MethodFactory {

	private final boolean singleton;
	private final TypeMirror returnType;

	public GetMethod(boolean singleton, TypeMirror returnType) {
		this.singleton = singleton;
		this.returnType = returnType;
	}

	@Override
	public void append(TypeSpec.Builder builder, AbstractClassEntity<?> entity) {
		List<Modifier> getMethodModifier = new ArrayList<>(List.of(Modifier.PUBLIC, Modifier.FINAL));

		CodeBlock.Builder getCodeBlock = CodeBlock.builder();
		if (singleton) {
			getCodeBlock.addStatement("return instance.getOrSet(() -> createInstance(wireRepository))");

			builder.addField(
					FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(SafeReference.class), TypeName.get(entity.rootType())), "instance")
							.addModifiers(Modifier.PRIVATE, Modifier.FINAL)
							.initializer("$T.empty()", SafeReference.class)
							.build()
			);
			getMethodModifier.add(Modifier.SYNCHRONIZED);
		} else {
			getCodeBlock.addStatement("return createInstance(wireRepository)");
		}

		builder.addMethod(
				MethodSpec.methodBuilder("get")
						.addModifiers(getMethodModifier)
						.addAnnotation(Override.class)
						.addParameter(
								ParameterSpec.builder(WireRepository.class, "wireRepository", Modifier.FINAL)
										.addAnnotation(NotNull.class)
										.build()
						).addCode(getCodeBlock.build())
						.returns(TypeName.get(returnType))
						.build());
	}
}
