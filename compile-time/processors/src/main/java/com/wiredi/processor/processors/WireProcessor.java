package com.wiredi.processor.processors;

import com.google.auto.service.AutoService;
import com.wiredi.annotations.Wire;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.processor.lang.processors.WireBaseProcessor;
import com.wiredi.processor.processors.adapter.IdentifiableProviderWireAdapter;
import jakarta.inject.Inject;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@AutoService(Processor.class)
public class WireProcessor extends WireBaseProcessor {

	@Inject
	private Logger logger;

	@Inject
	private IdentifiableProviderWireAdapter adapter;

	@Override
	protected List<Class<? extends Annotation>> targetAnnotations() {
		return Collections.singletonList(Wire.class);
	}

	@Override
	protected void handle(Element element) {
		if (!element.getKind().isClass()) {
			logger.error(element, "The annotated element of Wire has to be a class!");
			return;
		}
		final TypeElement typeElement = (TypeElement) element;
		Optional<Wire> wireAnnotation = Annotations.getFrom(typeElement, Wire.class);
		if (wireAnnotation.isEmpty()) {
			logger.warn(element, "Failed to find a WireAnnotation. This is likely an error with the algorithm to determine inherited annotations.");
		}

		adapter.handle(typeElement, wireAnnotation.orElse(null));
	}
}
