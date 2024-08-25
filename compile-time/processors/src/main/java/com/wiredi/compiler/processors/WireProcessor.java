package com.wiredi.compiler.processors;

import com.google.auto.service.AutoService;
import com.wiredi.annotations.Wire;
import com.wiredi.annotations.stereotypes.AutoConfiguration;
import com.wiredi.annotations.stereotypes.Configuration;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.domain.ClassEntity;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.compiler.processor.lang.processors.WireBaseProcessor;
import com.wiredi.compiler.processor.plugins.CompilerEntityPlugin;
import com.wiredi.compiler.processors.adapter.IdentifiableProviderWireAdapter;
import com.wiredi.compiler.processors.adapter.InterfaceImplementationAdapter;
import jakarta.inject.Inject;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

@AutoService(Processor.class)
public class WireProcessor extends WireBaseProcessor {

	@Inject
	private Logger logger;

	@Inject
	private IdentifiableProviderWireAdapter wireAdapter;

	@Inject
	private InterfaceImplementationAdapter interfaceImplementationAdapter;

	@Override
	protected List<Class<? extends Annotation>> targetAnnotations() {
		return List.of(Wire.class, Configuration.class, AutoConfiguration.class);
	}

	@Override
	protected void handle(Element element) {
		logger.debug("Handling " + element);
		if (!(element instanceof TypeElement typeElement)) {
			logger.error(element, "The annotated element of Wire has to be a class!");
			return;
		}
        Optional<Wire> wireAnnotation = Annotations.getAnnotation(typeElement, Wire.class);
		if (wireAnnotation.isEmpty()) {
			logger.warn(element, "Failed to find a WireAnnotation. This is likely an error with the algorithm to determine inherited annotations.");
		}

		if (element.getKind().isInterface()) {
			interfaceImplementationAdapter.handle(typeElement, wireAnnotation.orElse(null));
		} else {
			wireAdapter.handle(typeElement, wireAnnotation.orElse(null));
		}
	}
}
