package com.wiredi.compiler.domain;

import com.squareup.javapoet.CodeBlock;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.domain.provider.TypeIdentifier;
import jakarta.inject.Inject;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

public class TypeIdentifiers {

	private static Logger logger = Logger.get(TypeIdentifier.class);

	@Inject
	private Types types;

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
}
