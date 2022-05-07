package com.github.thorbenkuck.di.processor.builder;

import com.github.thorbenkuck.di.processor.WireInformation;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;

public class CompositionProxyBuilder extends ClassBuilder {
	protected CompositionProxyBuilder(WireInformation wireInformation) {
		super(wireInformation);
	}

	@Override
	protected TypeSpec.Builder initialize() {
		return TypeSpec.classBuilder(wireInformation.getSuggestedRoot().getSimpleName() + "$$CompositionProxy")
				.superclass(ClassName.get(wireInformation.getSuggestedRoot()))
				.addModifiers(Modifier.FINAL);
	}
}
