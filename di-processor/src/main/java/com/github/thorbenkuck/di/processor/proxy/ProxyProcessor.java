package com.github.thorbenkuck.di.processor.proxy;

import com.github.thorbenkuck.di.annotations.Proxy;
import com.github.thorbenkuck.di.processor.ClassWriter;
import com.github.thorbenkuck.di.processor.foundation.DiProcessor;
import com.github.thorbenkuck.di.processor.foundation.ProcessingException;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;

@AutoService(Processor.class)
public class ProxyProcessor extends DiProcessor {
	@Override
	protected Collection<Class<? extends Annotation>> supportedAnnotations() {
		return Collections.singleton(Proxy.class);
	}

	@Override
	protected void handle(Element element) {
		if (!(element instanceof TypeElement)) {
			throw new ProcessingException(element, "Only TypeElements may be annotated with @Proxy");
		}
		TypeElement typeElement = (TypeElement) element;
		Proxy proxy = element.getAnnotation(Proxy.class);

		TypeSpec.Builder builder = new ProxyBuilder(logger)
				.basedOnTypeElement(typeElement)
				.wireWith(proxy.wire())
				.withName(proxy.name())
				.overrideConstructors()
				.overridePublicMethods()
				.overrideProtectedMethods()
				.overridePackagePrivateMethods()
				.builder();

		markAsGenerated(builder, "This file is a Proxy of the type " + element.getSimpleName());

		ClassWriter.write(builder.build(), typeElement);

		markAsProcessed(element);
	}
}
