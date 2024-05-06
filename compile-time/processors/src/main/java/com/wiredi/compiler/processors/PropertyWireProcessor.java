package com.wiredi.compiler.processors;

import com.google.auto.service.AutoService;
import com.wiredi.annotations.properties.PropertyBinding;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.compiler.processor.factories.IdentifiableProviderFactory;
import com.wiredi.compiler.processor.lang.processors.WireBaseProcessor;
import jakarta.inject.Inject;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@AutoService(Processor.class)
public class PropertyWireProcessor extends WireBaseProcessor {

	@Inject
	private Logger logger;

	@Inject
	private IdentifiableProviderFactory factory;

	@Override
	protected List<Class<? extends Annotation>> targetAnnotations() {
		return Collections.singletonList(PropertyBinding.class);
	}

	@Override
	protected void handle(Element element) {
		if (!element.getKind().isClass()) {
			logger.error(element, "The annotated element of PropertySource has to be a class!");
			return;
		}
		final TypeElement typeElement = (TypeElement) element;
		Optional<PropertyBinding> propertyBinding = Annotations.getAnnotation(typeElement, PropertyBinding.class);
		if (propertyBinding.isEmpty()) {
			logger.error(element, "Failed to find a PropertyBinding annotation!");
			return;
		}

		if (factory.create(typeElement, propertyBinding.get()) == null) {
			logger.error(typeElement, () -> "The factory did not successfully create an IdentifiableProvider");
		}
	}
}
