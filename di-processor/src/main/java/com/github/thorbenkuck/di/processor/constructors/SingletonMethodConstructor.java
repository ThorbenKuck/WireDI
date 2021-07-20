package com.github.thorbenkuck.di.processor.constructors;

import com.github.thorbenkuck.di.annotations.Wire;
import com.github.thorbenkuck.di.processor.util.MethodCreator;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.TypeElement;

public class SingletonMethodConstructor implements MethodConstructor {
    @Override
    public void construct(TypeElement typeElement, TypeSpec.Builder typeBuilder) {
        boolean singleton = typeElement.getAnnotation(Wire.class).singleton();

        MethodSpec singletonMethod = MethodCreator.createSimpleBooleanMethod("singleton", singleton);

        typeBuilder.addMethod(singletonMethod);
    }
}
