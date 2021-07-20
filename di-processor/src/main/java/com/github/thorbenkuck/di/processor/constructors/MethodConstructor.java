package com.github.thorbenkuck.di.processor.constructors;

import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.TypeElement;

public interface MethodConstructor {

    void construct(TypeElement typeElement, TypeSpec.Builder typeBuilder);

}
