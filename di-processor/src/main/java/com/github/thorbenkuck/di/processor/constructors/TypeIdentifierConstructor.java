package com.github.thorbenkuck.di.processor.constructors;

import com.github.thorbenkuck.di.annotations.Wire;
import com.github.thorbenkuck.di.processor.foundation.Logger;
import com.github.thorbenkuck.di.processor.foundation.ProcessingException;
import com.github.thorbenkuck.di.processor.util.TypeElementAnalyzer;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.stream.Collectors;

public class TypeIdentifierConstructor implements MethodConstructor {

	private final Types types;
	private final TypeElementAnalyzer typeElementAnalyzer;
	private final Logger logger;

	public TypeIdentifierConstructor(Types types, Logger logger) {
		this.types = types;
		this.logger = logger;
		typeElementAnalyzer = new TypeElementAnalyzer(types);
	}

	@Override
	public void construct(TypeElement typeElement, TypeSpec.Builder builder) {
		builder.addMethod(buildTypeMethod(typeElement));
		builder.addMethod(buildWiredTypesMethod(typeElement, builder));
	}

	private MethodSpec buildTypeMethod(TypeElement typeElement) {
		logger.log("Constructing \"type\" method");
		return MethodSpec.methodBuilder("type")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.returns(ClassName.get(Class.class))
				.addCode(CodeBlock.builder().addStatement("return $T.class", ClassName.get(typeElement)).build())
				.build();
	}

	private MethodSpec buildWiredTypesMethod(TypeElement typeElement, TypeSpec.Builder builder) {
		logger.log("Constructing \"wiredTypes\" method");
		MethodSpec.Builder wiredTypesMethodBuilder = MethodSpec.methodBuilder("wiredTypes")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.returns(TypeName.get(Class[].class));

		Wire annotation = typeElement.getAnnotation(Wire.class);

		try {
			annotation.to();
		} catch (javax.lang.model.type.MirroredTypesException e) {
			List<? extends TypeMirror> typeMirrors = e.getTypeMirrors();

			if (typeMirrors.isEmpty()) {
				buildFromAnnotatedTypeElement(typeElement, builder, wiredTypesMethodBuilder);
			} else {
				for(TypeMirror typeMirror : typeMirrors) {
					if(!types.isAssignable(typeElement.asType(), typeMirror)) {
						throw new ProcessingException(typeElement, "The annotated element " + typeElement + " is not assignable to " + typeMirror);
					}
				}

				takeFromAnnotation(builder, wiredTypesMethodBuilder, typeMirrors);
			}
		}

		MethodSpec build = wiredTypesMethodBuilder.build();
		logger.log(build.toString());
		logger.log("Build WiredTypes method");
		return build;
	}

	private void buildFromAnnotatedTypeElement(
			TypeElement typeElement,
			TypeSpec.Builder builder,
			MethodSpec.Builder wiredTypesBuilder
	) {
		List<ClassName> elements = typeElementAnalyzer.getSuperElements(typeElement.asType())
				.stream()
				.map(ClassName::get)
				.collect(Collectors.toList());
		elements.add(ClassName.get(typeElement));

		buildTypeIdentifierMethod(wiredTypesBuilder, builder, elements);
	}

	private void takeFromAnnotation(
			TypeSpec.Builder builder,
			MethodSpec.Builder wiredTypesMethodBuilder,
			List<? extends TypeMirror> typeMirrors
	) {
		List<TypeName> classNames = typeMirrors.stream()
				.map(ClassName::get)
				.collect(Collectors.toList());
		buildTypeIdentifierMethod(wiredTypesMethodBuilder, builder, classNames);
	}

	private void buildTypeIdentifierMethod(
			MethodSpec.Builder wiredTypesBuilder,
			TypeSpec.Builder builder,
			List<? extends TypeName> typeNames
	) {
		CodeBlock.Builder identifierMethodBody = CodeBlock.builder()
				.add("new Class[]{")
				.add("$T.class", typeNames.remove(0));

		typeNames.forEach(name -> identifierMethodBody.add(", ").add("$T.class", name));

		identifierMethodBody.add("}");

		builder.addField(FieldSpec.builder(Class[].class, "types")
				.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
				.initializer(identifierMethodBody.build())
				.build());

		wiredTypesBuilder.addCode(CodeBlock.builder()
				.addStatement("return types")
				.build());
	}
}
