package com.wiredi.processor.lang;

import com.wiredi.annotations.Wire;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.compiler.repository.CompilerRepository;
import com.wiredi.compiler.processor.business.AspectAwareProxyService;
import com.wiredi.compiler.processor.business.InjectionPointService;
import com.wiredi.compiler.processor.lang.processors.WireBaseProcessor;
import jakarta.inject.Inject;

import javax.lang.model.element.Element;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

//@AutoService(Processor.class)
public class ExampleProcessor extends WireBaseProcessor {

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
	protected List<Class<? extends Annotation>> targetAnnotations() {
		return Collections.singletonList(Wire.class);
	}

	@Override
	protected void handle(Element element) {
	}

}
