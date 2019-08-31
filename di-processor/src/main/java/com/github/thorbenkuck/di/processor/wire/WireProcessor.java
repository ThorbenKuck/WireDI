package com.github.thorbenkuck.di.processor.wire;

import com.github.thorbenkuck.di.annotations.Wire;
import com.github.thorbenkuck.di.processor.ClassWriter;
import com.github.thorbenkuck.di.processor.IdentifiableProviderConstructor;
import com.github.thorbenkuck.di.processor.foundation.DiProcessor;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;

@AutoService(Processor.class)
public class WireProcessor extends DiProcessor {

	@Override
	protected Collection<Class<? extends Annotation>> supportedAnnotations() {
		return Collections.singletonList(Wire.class);
	}

	@Override
	protected void handle(Element element) {
		if (element.getKind() != ElementKind.CLASS) {
			logger.error("The annotated element of Wire has to be a class!", element);
			return;
		}

		if(element.getAnnotation(Wire.class) == null) {
			logger.error("Meta annotations are currently not supported!", element);
			return;
		}

		TypeElement typeElement = (TypeElement) element;
		IdentifiableProviderConstructor identifiableProviderConstructor = new IdentifiableProviderConstructor(typeElement, types, logger);
		identifiableProviderConstructor.addConsumer(builder -> markAsGenerated(builder, "This class is used to identify wired components"));
		TypeSpec construct = identifiableProviderConstructor.construct();

		ClassWriter.write(construct, typeElement);

		markAsProcessed(element);
	}
}
