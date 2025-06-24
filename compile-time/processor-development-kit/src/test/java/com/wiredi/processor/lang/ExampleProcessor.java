package com.wiredi.processor.lang;

import com.wiredi.annotations.Wire;
import org.slf4j.Logger;import com.wiredi.compiler.processor.lang.ProcessingElement;
import com.wiredi.compiler.processor.lang.AnnotationProcessorSubroutine;
import com.wiredi.compiler.repository.CompilerRepository;
import com.wiredi.compiler.processor.business.AspectAwareProxyService;
import com.wiredi.compiler.processor.business.InjectionPointService;
import jakarta.inject.Inject;

import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.List;

//@AutoService(Processor.class)
public class ExampleProcessor implements AnnotationProcessorSubroutine {

	@Inject
	Logger logger;

	@Inject
	CompilerRepository compilerRepository;

	@Inject
	Types types;

	@Inject
	InjectionPointService injectionPointService;

	@Inject
	AspectAwareProxyService proxyService;

	@Override
	public List<Class<? extends Annotation>> targetAnnotations() {
		return List.of(Wire.class);
	}

	@Override
	public void handle(ProcessingElement element) {
	}

}
