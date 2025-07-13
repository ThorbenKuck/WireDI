package com.wiredi.compiler.domain;

import com.squareup.javapoet.CodeBlock;
import com.wiredi.runtime.qualifier.QualifierType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.values.Value;
import jakarta.inject.Inject;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class TypeIdentifiers {

	private static Logger logger = org.slf4j.LoggerFactory.getLogger(TypeIdentifier.class);

	@Inject
	private Types types;

	@Inject
	private Elements elements;

	private Value<TypeMirror> objectTypeMirror = Value.lazy(() -> elements.getTypeElement(Object.class.getName()).asType());

	public CodeBlock newTypeIdentifier(TypeElement typeElement) {
		return newTypeIdentifier(typeElement.asType());
	}

	public CodeBlock newTypeIdentifier(TypeMirror typeMirror) {
		CodeBlock.Builder builder = CodeBlock.builder()
				.add("$T.of($T.class)", TypeIdentifier.class, types.erasure(typeMirror));
		var indented = false;

		if (typeMirror instanceof DeclaredType declaredType) {
			for (TypeMirror argument : declaredType.getTypeArguments()) {
				if (argument.getKind() == TypeKind.TYPEVAR) {
					continue;
				}
				if (!indented) {
					builder.indent();
					indented = true;
				}
				builder.add("\n").add(".withGeneric($L)", newTypeIdentifier(argument));
			}
		}

		if (indented) {
			builder.unindent();
		}

		return builder.build();
	}

	@NotNull
	public CodeBlock newTypeIdentifier(@NotNull TypeMirror typeMirror, @Nullable QualifierType qualifierType) {
		if (qualifierType == null) {
			return newTypeIdentifier(typeMirror);
		}
		CodeBlock.Builder builder = CodeBlock.builder()
				.add("$T.of($T.class)", TypeIdentifier.class, types.erasure(typeMirror));
		builder.indent();

		if (typeMirror instanceof DeclaredType declaredType) {
			for (TypeMirror argument : declaredType.getTypeArguments()) {
				if (argument.getKind() == TypeKind.TYPEVAR) {
					continue;
				}
				builder.add("\n").add(".withGeneric($L)", newTypeIdentifier(argument));
			}
		}

		builder.add("\n.qualified(")
				.indent()
				.add("\n$T.builder($S)", QualifierType.class, qualifierType.name());
		qualifierType.forEach((key, value) -> {
			builder.add("\n.add($S, ", key);
            switch (value) {
                case String s -> builder.add("$S", s);
                case Character character -> builder.add("'$L'", character);
                case Class<?> c -> builder.add("$S", c.getName());
                default -> builder.add("$L", value);
            }
			builder.add("\n)");
		});

		return builder.add("\n.build()")
				.unindent()
				.add(")")
				.unindent()
				.build();
	}

	public TypeMirror objectType() {
		return objectTypeMirror.get();
	}
}
