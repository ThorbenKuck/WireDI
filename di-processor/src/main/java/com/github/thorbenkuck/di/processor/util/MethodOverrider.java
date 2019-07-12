package com.github.thorbenkuck.di.processor.util;

import com.github.thorbenkuck.di.annotations.ProxyPreventAnnotations;
import com.github.thorbenkuck.di.processor.foundation.ProcessingException;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.*;
import java.util.ArrayList;
import java.util.List;

public class MethodOverrider {

	public static MethodSpec.Builder delegateMethod(ExecutableElement method, CodeBlock... codeBlocks) {
		if (!method.getModifiers().contains(Modifier.PRIVATE) &&
				!method.getModifiers().contains(Modifier.FINAL)) {
			MethodSpec.Builder overriding = MethodSpec.overriding(method);
			if(method.getAnnotation(ProxyPreventAnnotations.class) == null) {
				for (AnnotationMirror annotationMirror : method.getAnnotationMirrors()) {
					overriding.addAnnotation(AnnotationSpec.get(annotationMirror));
				}
			}

			for(CodeBlock codeBlock : codeBlocks) {
				overriding.addCode(codeBlock);
			}
			List<? extends VariableElement> parameters = method.getParameters();
			if (parameters.isEmpty()) {
				overriding.addStatement("super.$L()", method.getSimpleName());
			} else {
				StringBuilder stringBuilder = new StringBuilder(parameters.get(0).getSimpleName());

				for (int i = 1; i < parameters.size(); i++) {
					stringBuilder.append(", ").append(parameters.get(i).getSimpleName());
				}

				overriding.addStatement("super.$L($L)", method.getSimpleName(), stringBuilder.toString());
			}
			return overriding;
		} else {
			throw new ProcessingException(method, "Not overriding the method " + method.getSimpleName() + " because of its modifiers");
		}
	}

	public static MethodSpec.Builder delegateConstructor(ExecutableElement constructor) {

		MethodSpec.Builder builder = MethodSpec.constructorBuilder()
				.addModifiers(constructor.getModifiers());

		List<String> names = new ArrayList<>();

		int count = 0;
		for (VariableElement parameter : constructor.getParameters()) {
			String name = "t_" + count++;
			builder.addParameter(TypeName.get(parameter.asType()), name);
			names.add(name);
		}

		if(names.isEmpty()) {
			builder.addStatement("super()");
		} else {
			StringBuilder stringBuilder = new StringBuilder(names.get(0));

			for(int i = 1 ; i < names.size() ; i++) {
				stringBuilder.append(", ").append(names.get(i));
			}

			builder.addStatement("super($L)", stringBuilder.toString());
		}

		return builder;
	}

}
