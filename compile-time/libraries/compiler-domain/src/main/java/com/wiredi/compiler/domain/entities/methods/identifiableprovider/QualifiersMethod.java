package com.wiredi.compiler.domain.entities.methods.identifiableprovider;

import com.squareup.javapoet.*;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.entities.methods.MethodFactory;
import com.wiredi.qualifier.QualifierType;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Modifier;
import java.util.List;

public class QualifiersMethod implements MethodFactory {

	private final List<QualifierType> qualifierTypes;

	public QualifiersMethod(List<QualifierType> qualifierTypes) {
		this.qualifierTypes = qualifierTypes;
	}

	@Override
	public void append(
			TypeSpec.Builder builder,
			AbstractClassEntity<?> entity
	) {
		if (qualifierTypes.isEmpty()) {
			return;
		}
		final String fieldName = "QUALIFIER";
		List<CodeBlock> values = qualifierTypes.stream()
				.map(this::qualifierValueBuilder)
				.toList();

		builder.addField(
				FieldSpec.builder(ParameterizedTypeName.get(List.class, QualifierType.class), fieldName)
						.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
						.addAnnotation(NotNull.class)
						.initializer(
								CodeBlock.builder()
										.add("$T.of(\n", List.class).indent()
										.add(CodeBlock.join(values, ",\n"))
										.unindent().add("\n)")
										.build()
						).build()
		).addMethod(
				MethodSpec.methodBuilder("qualifiers")
						.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
						.addAnnotation(NotNull.class)
						.addAnnotation(Override.class)
						.returns(ParameterizedTypeName.get(List.class, QualifierType.class))
						.addStatement("return $L", fieldName)
						.build()
		);
	}

	private CodeBlock qualifierValueBuilder(QualifierType qualifier) {
		if (qualifier.values().isEmpty()) {
			return CodeBlock.builder().add("$T.just($S)", QualifierType.class, qualifier.name()).build();
		} else {
			CodeBlock.Builder builder = CodeBlock.builder()
					.add("$T.newInstance($S)", QualifierType.class, qualifier.name())
					.indent();
			qualifier.forEach((key, value) -> {
				builder.add("\n.add($S, ", key);
				if (value instanceof String) {
					builder.add("$S", value);
				} else if (value instanceof Character) {
					builder.add("'$L'", value);
				} else if (value instanceof Class<?> c) {
					builder.add("$S", c.getName());
				} else {
					builder.add("$L", value);
				}
				builder.add(")");
			});
			return builder.add("\n.build()").unindent().build();
		}
	}
}
