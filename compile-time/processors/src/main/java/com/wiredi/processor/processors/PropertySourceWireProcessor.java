package com.wiredi.processor.processors;

import com.google.auto.service.AutoService;
import com.wiredi.annotations.properties.PropertySource;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.processor.factories.EnvironmentConfigurationFactory;
import com.wiredi.processor.lang.processors.WireBaseProcessor;
import jakarta.inject.Inject;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@AutoService(Processor.class)
public class PropertySourceWireProcessor extends WireBaseProcessor {

	@Inject
	private Logger logger;

	@Inject
	private EnvironmentConfigurationFactory factory;

	@Override
	protected List<Class<? extends Annotation>> targetAnnotations() {
		return Collections.singletonList(PropertySource.class);
	}

	@Override
	protected void handle(Element element) {
		if (!element.getKind().isClass()) {
			logger.error(element, "The annotated element of PropertySource has to be a class!");
			return;
		}
		final TypeElement typeElement = (TypeElement) element;
		Optional<PropertySource> wireAnnotation = Annotations.getFrom(typeElement, PropertySource.class);
		if (wireAnnotation.isEmpty()) {
			logger.error(element, "Failed to find a PropertySource annotation!");
			return;
		}

		if (factory.create(typeElement) == null) {
			logger.error(typeElement, () -> "The factory did not successfully create an EnvironmentConfiguration");
		}
	}
}
