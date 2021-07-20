package com.github.thorbenkuck.di.processor.constructors;

import com.github.thorbenkuck.di.ReflectionsHelper;
import com.github.thorbenkuck.di.Repository;
import com.github.thorbenkuck.di.annotations.Wire;
import com.github.thorbenkuck.di.processor.FetchAnnotated;
import com.github.thorbenkuck.di.processor.FieldInjector;
import com.github.thorbenkuck.di.processor.foundation.ProcessingException;
import com.github.thorbenkuck.di.processor.util.MethodCreator;
import com.github.thorbenkuck.di.processor.wire.ConstructorFinder;
import com.squareup.javapoet.*;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.lang.model.element.*;
import java.util.List;

public class GetAndLazyMethodConstructor implements MethodConstructor {

	@Override
	public void construct(TypeElement typeElement, TypeSpec.Builder typeBuilder) {
		List<VariableElement> annotatedInjectionFields = FetchAnnotated.fields(typeElement, Inject.class);

		typeBuilder.addField(FieldSpec.builder(TypeName.get(typeElement.asType()), "instance")
				.addModifiers(Modifier.PRIVATE)
				.build());

		CodeBlock.Builder aReturn = CodeBlock.builder();

		if (typeElement.getAnnotation(Singleton.class) != null) {
			aReturn.beginControlFlow("if(instance != null)")
					.addStatement("return instance")
					.endControlFlow();
		}

		aReturn.addStatement("instance = $L(wiredTypes)", ConstructorFinder.INSTANTIATION_METHOD_NAME);
		aReturn.add(FieldInjector.create(annotatedInjectionFields));
		applyPostConstructMethods(typeElement, aReturn);
		aReturn.addStatement("return instance");

		typeBuilder.addMethod(
				MethodSpec.methodBuilder("get")
						.addAnnotation(Override.class)
						.returns(TypeName.get(typeElement.asType()))
						.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
						.addParameter(ClassName.get(Repository.class), "wiredTypes")
						.addCode(aReturn.build())
						.build()
		);
	}

	private void applyPostConstructMethods(TypeElement typeElement, CodeBlock.Builder codeBlockBuilder) {
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
