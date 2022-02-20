package com.github.thorbenkuck.di.processor.processors;

import com.github.thorbenkuck.di.annotations.Wire;
import com.github.thorbenkuck.di.processor.WireInformation;
import com.github.thorbenkuck.di.processor.builder.AspectAwareProxyBuilder;
import com.github.thorbenkuck.di.processor.builder.IdentifiableProviderClassBuilder;
import com.github.thorbenkuck.di.processor.constructors.MethodConstructor;
import com.github.thorbenkuck.di.processor.foundation.DiProcessor;
import com.github.thorbenkuck.di.processor.foundation.Logger;
import com.github.thorbenkuck.di.processor.foundation.ProcessingException;
import com.google.auto.service.AutoService;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@AutoService(Processor.class)
public class WireDiProcessor extends DiProcessor {

	@Override
	protected Collection<Class<? extends Annotation>> supportedAnnotations() {
		return Collections.singletonList(Wire.class);
	}

	@Override
	protected void handle(Element element) {
		if (!element.getKind().isClass()) {
			Logger.error("The annotated element of Wire has to be a class!", element);
			return;
		}

		if(element.getAnnotation(Wire.class) == null) {
			Logger.error("Meta annotations are currently not supported!", element);
			return;
		}

		final TypeElement typeElement = (TypeElement) element;
		final WireInformation wireInformation = WireInformation.extractOf(typeElement);

		if(wireInformation.isProxyExpected()) {
			if(!AspectAwareProxyBuilder.eligibleForProxy(typeElement)) {
				throw new ProcessingException(typeElement, "This is not eligible for auto proxy.");
			}

			Objects.requireNonNull(wireInformation, "The WireInformation somehow become null");
			new AspectAwareProxyBuilder(wireInformation)
					.addDelegatingConstructors()
					.overwriteMethods()
					.appendWireAnnotations()
					.buildAndWrite("A Proxy: TODO Describe me");
		} else {
			new IdentifiableProviderClassBuilder(wireInformation)
					.overwriteAllRequiredMethods()
					.identifyingAWiredSource()
					.buildAndWrite("This class is used to identify wired components");
		}
	}
}
