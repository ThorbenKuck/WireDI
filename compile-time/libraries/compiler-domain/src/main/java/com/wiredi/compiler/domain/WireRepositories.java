package com.wiredi.compiler.domain;

import com.squareup.javapoet.CodeBlock;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.qualifier.QualifierType;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;

public class WireRepositories {

	private final TypeChecker typeChecker;
	private final TypeIdentifiers typeIdentifiers;
	private final Logger logger = Logger.get(WireRepositories.class);

	public WireRepositories(TypeChecker typeChecker, TypeIdentifiers typeIdentifiers) {
		this.typeChecker = typeChecker;
		this.typeIdentifiers = typeIdentifiers;
	}

	public CodeBlock fetchFromWireRepository(
			TypeElement element
	) {
		return fetchFromWireRepository(element.asType(), null);
	}

	public CodeBlock fetchFromWireRepository(
			Element element
	) {
		return fetchFromWireRepository(element.asType(), Qualifiers.injectionQualifier(element));
	}

	public CodeBlock fetchFromWireRepository(
			Element element,
			@Nullable QualifierType qualifierTypes
	) {
		return fetchFromWireRepository(element.asType(), qualifierTypes);
	}

	public CodeBlock fetchFromWireRepository(
			VariableElement variableElement,
			@Nullable QualifierType qualifierTypes
	) {
		return fetchFromWireRepository(variableElement.asType(), qualifierTypes);
	}

	public CodeBlock fetchFromWireRepository(
			TypeMirror typeMirror,
			@Nullable QualifierType qualifierType
	) {
		var codeBlock = CodeBlock.builder()
				.add("wireRepository.");
		List<CodeBlock> codeBlocks = new ArrayList<>();
		TypeChecker.Checker type = typeChecker.theType(typeMirror);

		if (type.isProvider()) {
			codeBlocks.add(typeIdentifiers.newTypeIdentifier(getGenericTypeOf(typeMirror)));
			codeBlock.add("getProvider");
		} else if (type.isNativeProvider()) {
			codeBlocks.add(typeIdentifiers.newTypeIdentifier(getGenericTypeOf(typeMirror)));
			codeBlock.add("getNativeProvider");
		} else if (type.isBean()) {
			codeBlocks.add(typeIdentifiers.newTypeIdentifier(getGenericTypeOf(typeMirror)));
			codeBlock.add("getBean");
		} else if (type.isCollection() || type.isList()) {
			codeBlocks.add(typeIdentifiers.newTypeIdentifier(getGenericTypeOf(typeMirror)));
			codeBlock.add("getAll");
		} else {
			codeBlocks.add(typeIdentifiers.newTypeIdentifier(typeMirror));
			codeBlock.add("get");
		}

		if (qualifierType != null) {
			codeBlocks.add(Qualifiers.qualifierValueBuilder(qualifierType));
		}

		if (codeBlocks.size() == 1) {
			return codeBlock.add("($L)", CodeBlock.join(codeBlocks, ", ")).build();
		} else {
			return codeBlock.add("(\n")
					.indent()
					.add(CodeBlock.join(codeBlocks, ",\n"))
					.unindent()
					.add("\n)")
					.build();
		}
	}

	private TypeMirror getGenericTypeOf(TypeMirror typeMirror) {
		if (!(typeMirror instanceof DeclaredType declared)) {
			throw new IllegalArgumentException("Only declared types are allowed");
		}
		return declared.getTypeArguments().get(0);
	}
}
