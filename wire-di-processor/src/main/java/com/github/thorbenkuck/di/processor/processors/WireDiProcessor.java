package com.github.thorbenkuck.di.processor.processors;

import com.github.thorbenkuck.di.annotations.Wire;
import com.github.thorbenkuck.di.processor.DiProcessor;
import com.github.thorbenkuck.di.processor.Logger;
import com.github.thorbenkuck.di.processor.WireAnnotationInformationExtractor;
import com.github.thorbenkuck.di.processor.WireInformation;
import com.google.auto.service.AutoService;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;

@AutoService(Processor.class)
public class WireDiProcessor extends DiProcessor {

	@Override
	protected Class<? extends Annotation> targetAnnotation() {
		return Wire.class;
	}

	@Override
	protected void handle(Element element) {
		if (!element.getKind().isClass()) {
			Logger.error(element, "The annotated element of Wire has to be a class!");
			return;
		}

		if (element.getAnnotation(Wire.class) == null) {
			Logger.error(element, "Meta annotations are currently not supported!");
			return;
		}

		final TypeElement typeElement = (TypeElement) element;
		final WireInformation wireInformation = WireAnnotationInformationExtractor.extractOf(typeElement);
		WireInformationBuilder.buildFor(wireInformation);
	}
}
