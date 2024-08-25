package com.wiredi.compiler.domain;

import com.google.common.base.Strings;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.runtime.properties.Key;
import com.wiredi.runtime.properties.exceptions.PropertyNotFoundException;
import com.wiredi.runtime.qualifier.QualifierType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;

public class WireRepositories {

	private final TypeChecker typeChecker;
	private final TypeIdentifiers typeIdentifiers;
	private final Types types;
	private final Logger logger = Logger.get(WireRepositories.class);

	public WireRepositories(TypeChecker typeChecker, TypeIdentifiers typeIdentifiers, Types types) {
		this.typeChecker = typeChecker;
		this.typeIdentifiers = typeIdentifiers;
		this.types = types;
	}

	public CodeBlock resolveFromEnvironment(
			Element element,
			String resolve
	) {
		return resolveFromEnvironment(element.asType(), resolve);
	}

	public CodeBlock resolveFromEnvironment(
			TypeMirror typeMirror,
			String resolve
	) {
		CodeBlock.Builder codeBlock = CodeBlock.builder()
				.add("wireRepository.environment().resolve");

		TypeChecker.Checker rootType = typeChecker.theType(typeMirror);
		if (rootType.isOf(String.class)) {
			codeBlock.add("($S)", resolve);
		} else if(rootType.isList()) {
			codeBlock.add("List");
			TypeChecker.Checker genericType = typeChecker.theType(((DeclaredType) typeMirror).getTypeArguments().get(0));

			if (genericType.isOf(String.class)) {
				codeBlock.add("($S)", resolve);
			} else {
				codeBlock.add("($S, $T.class)", resolve, genericType.typeName());
			}
		}

		return codeBlock.build();
	}

	public CodeBlock fetchFromWireRepository(
			TypeElement element
	) {
		return fetchFromWireRepository(element.asType(), null, false);
	}

	public CodeBlock fetchFromWireRepository(
			Element element
	) {
		return fetchFromWireRepository(element.asType(), Qualifiers.injectionQualifier(element), isNullable(element));
	}

	public CodeBlock fetchFromWireRepository(
			Element element,
			@Nullable QualifierType qualifierTypes
	) {
		return fetchFromWireRepository(element.asType(), qualifierTypes, isNullable(element));
	}

	public CodeBlock fetchFromWireRepository(
			VariableElement variableElement,
			@Nullable QualifierType qualifierTypes
	) {
		return fetchFromWireRepository(variableElement.asType(), qualifierTypes, isNullable(variableElement));
	}
	public CodeBlock fetchFromWireRepository(
			TypeMirror typeMirror,
			@Nullable QualifierType qualifierType
	) {
		return fetchFromWireRepository(typeMirror, qualifierType, false);
	}

	public CodeBlock fetchFromWireRepository(
			TypeMirror typeMirror,
			@Nullable QualifierType qualifierType,
			boolean nullable
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
			if(nullable) {
				codeBlock.add("tryGet");
			} else {
				codeBlock.add("get");
			}
		}

		if (qualifierType != null) {
			codeBlocks.add(Qualifiers.qualifierValueBuilder(qualifierType));
		}

		if (codeBlocks.size() == 1) {
			codeBlock.add("($L)", CodeBlock.join(codeBlocks, ", "));
		} else {
			codeBlock.add("(\n")
					.indent()
					.add(CodeBlock.join(codeBlocks, ",\n"))
					.unindent()
					.add("\n)");
		}

		if (nullable) {
			codeBlock.add(".orElse(null)");
		}

		return codeBlock.build();
	}

	private TypeMirror getGenericTypeOf(TypeMirror typeMirror) {
		if (typeMirror instanceof DeclaredType declared) {
			return declared.getTypeArguments().get(0);
		}
		throw new IllegalArgumentException("Only declared types are allowed");
	}

	public boolean isNullable(Element element) {
		if (element instanceof VariableElement) {
			return Annotations.hasByName(element, "Nullable");
		}

		return false;
	}

	public CodeBlock fetchFromProperties(@NotNull VariableElement element, String name, String defaultValue) {
		Key propertyName;
		if (name.isBlank()) {
			propertyName = Key.format(element.getSimpleName().toString());
		} else {
			propertyName = Key.format(name);
		}
		CodeBlock.Builder builder = CodeBlock.builder()
				.add("wireRepository.environment()\n")
				.indent();

		if (defaultValue.isBlank()) {
			builder.add(".getProperty($T.just($S), $T.class)\n", Key.class, propertyName.value(), ClassName.get(element.asType()))
					.add(".orElseThrow(() -> new $T($S))", PropertyNotFoundException.class, propertyName.value());
		} else {
			builder.add(".getPropertyOrResolve($T.just($S), $T.class, $S)", Key.class, propertyName.value(), ClassName.get(element.asType()), defaultValue);
		}

		return builder.unindent().build();
	}
}
