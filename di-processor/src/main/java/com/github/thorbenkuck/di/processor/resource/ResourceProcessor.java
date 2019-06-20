package com.github.thorbenkuck.di.processor.resource;

import com.github.thorbenkuck.di.annotations.Resource;
import com.github.thorbenkuck.di.processor.foundation.DiProcessor;
import com.google.auto.service.AutoService;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;

@AutoService(Processor.class)
public class ResourceProcessor extends DiProcessor {
	@Override
	protected Collection<Class<? extends Annotation>> supportedAnnotations() {
		return Collections.singleton(Resource.class);
	}

	@Override
	protected void handle(Element element) {
		// TODO Fill this class
	}
}
