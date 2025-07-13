package com.wiredi.compiler.processor.plugins;

import com.squareup.javapoet.*;
import com.wiredi.compiler.domain.ClassEntity;
import com.wiredi.compiler.domain.Qualifiers;
import com.wiredi.compiler.domain.entities.methods.StandaloneMethodFactory;
import com.wiredi.runtime.qualifier.QualifierType;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Modifier;
import java.util.List;

public class QualifiersMethod implements StandaloneMethodFactory {

	private final List<QualifierType> qualifierTypes;

	public QualifiersMethod(List<QualifierType> qualifierTypes) {
		this.qualifierTypes = qualifierTypes;
	}

	@Override
	public void append(
			MethodSpec.@NotNull Builder builder,
			@NotNull ClassEntity<?> entity
	) {
		final String fieldName = "QUALIFIER";
		List<CodeBlock> values = qualifierTypes.stream()
				.map(Qualifiers::qualifierValueBuilder)
				.toList();

		entity.addField(ParameterizedTypeName.get(List.class, QualifierType.class), fieldName, field -> field.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
				.addAnnotation(NotNull.class)
				.initializer(
						CodeBlock.builder()
								.add("$T.of(\n", List.class).indent()
								.add(CodeBlock.join(values, ",\n"))
								.unindent().add("\n)")
								.build()
				));

		builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addAnnotation(NotNull.class)
				.addAnnotation(Override.class)
				.returns(ParameterizedTypeName.get(List.class, QualifierType.class))
				.addStatement("return $L", fieldName);
	}

	@Override
	public @NotNull String methodName() {
		return "qualifiers";
	}

	@Override
	public boolean applies(@NotNull ClassEntity<?> entity) {
		return !qualifierTypes.isEmpty();
	}
}
