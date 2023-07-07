package com.wiredi.processor.processors;

import com.google.auto.service.AutoService;
import com.wiredi.annotations.Wire;
import com.wiredi.annotations.external.WireCandidate;
import com.wiredi.annotations.external.WireConfiguration;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.processor.lang.processors.WireBaseProcessor;
import com.wiredi.processor.processors.adapter.IdentifiableProviderWireAdapter;
import jakarta.inject.Inject;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.List;

@AutoService(Processor.class)
public class WireConfigurationAnnotationProcessor extends WireBaseProcessor {

	private static final Logger logger = Logger.get(WireConfigurationAnnotationProcessor.class);

	@Inject
	private Types types;

	@Inject
	private IdentifiableProviderWireAdapter adapter;

	@Override
	protected List<Class<? extends Annotation>> targetAnnotations() {
		return List.of(WireConfiguration.class);
	}

	@Override
	protected void handle(Element element) {
		WireConfiguration annotation = element.getAnnotation(WireConfiguration.class);
		WireCandidate[] value = annotation.value();

		for (WireCandidate wireCandidate : value) {
			try {
				wireCandidate.value();
			} catch (MirroredTypeException mirroredTypeException) {
				Element targetElement = types.asElement(mirroredTypeException.getTypeMirror());
				doHandle(targetElement, wireCandidate.wire());
			}
		}
	}

	private void doHandle(Element element, Wire wireAnnotation) {
		if (!element.getKind().isClass()) {
			logger.error(element, "The annotated element of Wire has to be a class!");
			return;
		}
		final TypeElement typeElement = (TypeElement) element;
		adapter.handle(typeElement, wireAnnotation);
	}
}
