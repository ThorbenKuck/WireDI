package com.github.thorbenkuck.di.processor.processors;

import com.github.thorbenkuck.di.annotations.Provider;
import com.github.thorbenkuck.di.processor.WireDiProcessor;
import com.github.thorbenkuck.di.processor.Logger;
import com.github.thorbenkuck.di.processor.WireAnnotationInformationExtractor;
import com.github.thorbenkuck.di.processor.WireInformation;
import com.google.auto.service.AutoService;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

@AutoService(Processor.class)
public class ProviderProcessorWire extends WireDiProcessor {
	@Override
	protected List<Class<? extends Annotation>> targetAnnotations() {
		return Collections.singletonList(Provider.class);
	}

	@Override
	protected void handle(Element element) {
		if (element.getKind() != ElementKind.METHOD) {
			Logger.error(element, "The annotated element of Provider has to be a method!");
			return;
		}

		final ExecutableElement typeElement = (ExecutableElement) element;
		final WireInformation wireInformation = WireAnnotationInformationExtractor.extractForProvider(typeElement);
		WireInformationWriter.buildAndWriteFor(wireInformation);
	}
}
