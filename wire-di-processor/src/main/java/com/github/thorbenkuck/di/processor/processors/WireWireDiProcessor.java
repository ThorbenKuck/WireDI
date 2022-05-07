package com.github.thorbenkuck.di.processor.processors;

import com.github.thorbenkuck.di.annotations.Wire;
import com.github.thorbenkuck.di.processor.Logger;
import com.github.thorbenkuck.di.processor.WireAnnotationInformationExtractor;
import com.github.thorbenkuck.di.processor.WireInformation;
import com.google.auto.service.AutoService;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

@AutoService(Processor.class)
public class WireWireDiProcessor extends com.github.thorbenkuck.di.processor.WireDiProcessor {

	@Override
	protected List<Class<? extends Annotation>> targetAnnotations() {
		return Collections.singletonList(Wire.class);
	}

	@Override
	protected void handle(Element element) {
		if (!element.getKind().isClass()) {
			Logger.error(element, "The annotated element of Wire has to be a class!");
			return;
		}

		final TypeElement typeElement = (TypeElement) element;
		final WireInformation wireInformation = WireAnnotationInformationExtractor.extractOf(typeElement);
		WireInformationWriter.buildAndWriteFor(wireInformation);
	}
}
