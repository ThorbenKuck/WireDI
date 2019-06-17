package com.github.thorbenkuck.di.processor.wire;

import com.github.thorbenkuck.di.ReflectionsHelper;
import com.github.thorbenkuck.di.Repository;
import com.github.thorbenkuck.di.annotations.Wire;
import com.github.thorbenkuck.di.processor.FetchAnnotated;
import com.github.thorbenkuck.di.processor.FieldInjector;
import com.github.thorbenkuck.di.processor.foundation.ProcessingException;
import com.github.thorbenkuck.di.processor.MethodCreator;
import com.squareup.javapoet.*;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.lang.model.element.*;
import java.util.ArrayList;
import java.util.List;

public class GetAndLazyMethodConstructor {

	private final TypeElement typeElement;

	public GetAndLazyMethodConstructor(TypeElement typeElement) {
		this.typeElement = typeElement;
	}

	public void analyze(TypeSpec.Builder typeBuilder) {
		boolean lazy = typeElement.getAnnotation(Wire.class).lazy();
		List<VariableElement> annotatedInjectionFields = FetchAnnotated.fields(typeElement, Inject.class);

		MethodSpec lazyMethod = MethodCreator.createSimpleBooleanMethod("lazy", lazy);

		typeBuilder.addField(FieldSpec.builder(TypeName.get(typeElement.asType()), "instance")
				.addModifiers(Modifier.PRIVATE)
				.build());

		typeBuilder.addMethod(MethodCreator.createReturningMethod("get", TypeName.get(typeElement.asType()), "instance"));

		CodeBlock.Builder aReturn = CodeBlock.builder();

		if (typeElement.getAnnotation(Singleton.class) != null) {
			aReturn.beginControlFlow("if(instance != null)")
					.addStatement("return")
					.endControlFlow();
		}

		aReturn.addStatement("instance = $L(wiredTypes)", ConstructorFinder.INSTANTIATION_METHOD_NAME);
		aReturn.add(FieldInjector.createCode(annotatedInjectionFields));
		applyPostConstructMethods(aReturn);

		typeBuilder.addMethod(MethodSpec.methodBuilder("instantiate")
				.addAnnotation(Override.class)
				.addParameter(Repository.class, "wiredTypes")
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addCode(aReturn.build())
				.build());


		typeBuilder.addMethod(lazyMethod);
	}

	private void applyPostConstructMethods(CodeBlock.Builder codeBlockBuilder) {
		ExecutableElement postConstruct = null;
		for(Element element : typeElement.getEnclosedElements()) {
			if(element.getKind() == ElementKind.METHOD && element.getAnnotation(PostConstruct.class) != null) {
				if(postConstruct != null) {
					throw new ProcessingException(typeElement, "Only one Method may be annotated with @PostConstruct");
				}

				postConstruct = (ExecutableElement) element;
			}
		}

		if(postConstruct == null) {
			return;
		}

		if(!postConstruct.getParameters().isEmpty()) {
			throw new ProcessingException(typeElement, "No Arguments allowed on methods annotated with @PostConstruction");
		}

		if(postConstruct.getModifiers().contains(Modifier.PRIVATE)) {
			// TODO Log Warning
			codeBlockBuilder.addStatement("$T.invokeMethod(instance, $S)", ReflectionsHelper.class, postConstruct.getSimpleName().toString());
		} else {
			codeBlockBuilder.addStatement("instance.$L()", postConstruct.getSimpleName());
		}
	}
}
