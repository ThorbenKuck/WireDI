package com.github.thorbenkuck.di.processor.wire;

import com.github.thorbenkuck.di.DiInstantiationException;
import com.github.thorbenkuck.di.Repository;
import com.github.thorbenkuck.di.processor.ProcessingException;
import com.squareup.javapoet.*;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.lang.model.element.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConstructorFinder {

	public static final String INSTANTIATION_METHOD_NAME = "createInstance";
	private final TypeElement typeElement;
	private ExecutableElement chosenConstructor;

	public ConstructorFinder(TypeElement typeElement) {
		this.typeElement = typeElement;
	}

	private boolean isConstructor(Element element) {
		if (element instanceof ExecutableElement) {
			ExecutableElement executableElement = (ExecutableElement) element;
			return executableElement.getKind() == ElementKind.CONSTRUCTOR;
		}

		return false;
	}

	private MethodSpec.Builder instantiationMethodSpec(TypeElement typeElement) {
		return MethodSpec.methodBuilder(INSTANTIATION_METHOD_NAME)
				.addModifiers(Modifier.PRIVATE)
				.addParameter(Repository.class, "wiredTypes")
				.returns(TypeName.get(typeElement.asType()));
	}

	private ExecutableElement findBestSuited(List<ExecutableElement> constructors) {
		if (constructors.size() == 1) {
			return constructors.get(0);
		}
		ExecutableElement using = null;
		for (ExecutableElement constructor : constructors) {
			if (!(constructor.getAnnotation(Inject.class) == null)) {
				if (using != null) {
					throw new ProcessingException(typeElement, "You have to provide either one Constructor or annotated the constructor to use with javax.inject.Inject");
				}
				using = constructor;
			}
		}

		if (using == null) {
			throw new ProcessingException(typeElement, "You have to provide either one Constructor or annotated the constructor to use with javax.inject.Inject");
		}

		return using;
	}

	public MethodSpec.Builder forDefaultConstructor(TypeElement typeElement) {
		return instantiationMethodSpec(typeElement)
				.addCode(CodeBlock.builder()
						.addStatement("return new $T()", ClassName.get(typeElement))
						.build());
	}

	public MethodSpec.Builder withArguments(ExecutableElement constructor, TypeElement typeElement) {
		CodeBlock.Builder builder = CodeBlock.builder();
		List<String> names = new ArrayList<>();
		int i = 0;

		for (VariableElement argument : constructor.getParameters()) {
			String name = "t" + i++;
			builder.addStatement("$T $L = wiredTypes.getInstance($T.class)", ClassName.get(argument.asType()), name, ClassName.get(argument.asType()));
			if (argument.getAnnotation(Nullable.class) == null) {
				builder.beginControlFlow("if ($L == null) ", name)
						.addStatement("throw new $T($S)", DiInstantiationException.class, "Could not find any instance for " + ClassName.get(argument.asType()))
						.endControlFlow();
			}
			names.add(name);
		}

		StringBuilder argumentList = new StringBuilder(names.get(0));
		for (int count = 1; count < names.size(); count++) {
			argumentList.append(", ").append(names.get(count));
		}

		builder.addStatement("return new $T($L)", ClassName.get(typeElement), argumentList.toString());

		return instantiationMethodSpec(typeElement)
				.addCode(builder.build());
	}

	public void analyze(TypeSpec.Builder builder) {
		List<ExecutableElement> potentialConstructors = new ArrayList<>();
		for (Element element : typeElement.getEnclosedElements()) {
			if (isConstructor(element)) {
				potentialConstructors.add((ExecutableElement) element);
			}
		}

		if (potentialConstructors.isEmpty()) {
			builder.addMethod(forDefaultConstructor(typeElement).build());
		}

		ExecutableElement executableElement = findBestSuited(potentialConstructors);
		chosenConstructor = executableElement;
		if (executableElement.getParameters().isEmpty()) {
			builder.addMethod(forDefaultConstructor(typeElement).build());
		} else {
			builder.addMethod(withArguments(executableElement, typeElement).build());
		}
	}

	public List<? extends VariableElement> getChosenConstructor() {
		return chosenConstructor != null ? chosenConstructor.getParameters() : Collections.emptyList();
	}
}
