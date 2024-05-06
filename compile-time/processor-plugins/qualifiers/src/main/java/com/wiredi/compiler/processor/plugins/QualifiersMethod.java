package com.wiredi.compiler.processor.plugins;

import com.squareup.javapoet.*;
import com.wiredi.compiler.domain.ClassEntity;
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
			MethodSpec.Builder builder,
			ClassEntity<?> entity
	) {
		final String fieldName = "QUALIFIER";
		List<CodeBlock> values = qualifierTypes.stream()
				.map(this::qualifierValueBuilder)
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

	@Override
	public String methodName() {
		return "qualifiers";
	}

	@Override
	public boolean applies(ClassEntity<?> entity) {
		return !qualifierTypes.isEmpty();
	}
}
