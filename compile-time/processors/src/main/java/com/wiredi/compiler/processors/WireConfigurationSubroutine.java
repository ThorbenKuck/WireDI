package com.wiredi.compiler.processors;

import com.google.auto.service.AutoService;
import com.wiredi.annotations.Wire;
import com.wiredi.annotations.external.WireCandidate;
import com.wiredi.annotations.external.WireConfiguration;
import com.wiredi.compiler.logger.slf4j.CompileTimeLogger;
import com.wiredi.compiler.logger.slf4j.CompileTimeLoggerFactory;
import com.wiredi.compiler.processor.lang.ProcessingElement;
import com.wiredi.compiler.processor.lang.AnnotationProcessorSubroutine;
import com.wiredi.compiler.processors.adapter.IdentifiableProviderWireAdapter;
import jakarta.inject.Inject;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.List;

@AutoService(AnnotationProcessorSubroutine.class)
public class WireConfigurationSubroutine implements AnnotationProcessorSubroutine {

	private static final CompileTimeLogger logger = CompileTimeLoggerFactory.getLogger(WireConfigurationSubroutine.class);

	@Inject
	private Types types;
	@Inject
	private IdentifiableProviderWireAdapter adapter;

    @Override
	public List<Class<? extends Annotation>> targetAnnotations() {
		return List.of(WireConfiguration.class);
	}

	@Override
	public void handle(ProcessingElement processingElement) {
		Element element = processingElement.element();
		if (element.getKind() == ElementKind.ANNOTATION_TYPE) {
			return;
		}
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
