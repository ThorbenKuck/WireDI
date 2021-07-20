package com.github.thorbenkuck.di.processor.util;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;

public class MethodCreator {

	public static MethodSpec createSimpleBooleanMethod(String methodName, boolean returnValue) {
		return MethodSpec.methodBuilder(methodName)
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.returns(TypeName.BOOLEAN)
				.addCode(CodeBlock.builder().addStatement("return $L", returnValue)
						.build())
				.build();
	}

	public static MethodSpec createSimpleStringMethod(String methodName, String returnValue) {
		return MethodSpec.methodBuilder(methodName)
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.returns(String.class)
				.addCode(CodeBlock.builder().addStatement("return $S", returnValue)
						.build())
				.build();
	}

	public static MethodSpec.Builder createReturningMethod(String methodName, TypeName returnType, String returnName) {
		return MethodSpec.methodBuilder(methodName)
				.addAnnotation(Override.class)
				.returns(returnType)
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addStatement("return $L", returnName);
	}

}
