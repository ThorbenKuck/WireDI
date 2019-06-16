package com.github.thorbenkuck.di.processor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class DiProcessor extends AbstractProcessor {

	private final List<Element> doneProcessing = new ArrayList<>();
	protected Types types;
	protected Elements elements;
	protected Filer filer;
	protected Messager messager;

	protected abstract Collection<Class<? extends Annotation>> supportedAnnotations();

	@Override
	public final Set<String> getSupportedAnnotationTypes() {
		return supportedAnnotations().stream()
				.map(Class::getCanonicalName)
				.collect(Collectors.toSet());
	}

	@Override
	public final SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public synchronized final void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		types = processingEnv.getTypeUtils();
		elements = processingEnv.getElementUtils();
		filer = processingEnv.getFiler();
		messager = processingEnv.getMessager();
	}

	@Override
	public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for(Class<? extends Annotation> type : supportedAnnotations()) {
			for(Element element : roundEnv.getElementsAnnotatedWith(type)) {
				if(!hasBeenProcessed(element)) {
					try {
						handle(element);
					} catch (ProcessingException e) {
						error(e.getMessage(), e.getElement());
					}
				}
			}
		}
		return true;
	}

	protected abstract void handle(Element element);

	protected void error(String msg, Element element, AnnotationMirror mirror) {
		messager.printMessage(Diagnostic.Kind.ERROR, msg, element, mirror);
	}

	protected void error(String msg, Element element) {
		messager.printMessage(Diagnostic.Kind.ERROR, msg, element);
	}

	protected void error(String msg) {
		messager.printMessage(Diagnostic.Kind.ERROR, msg);
	}

	protected void log(String msg) {
		messager.printMessage(Diagnostic.Kind.NOTE, msg);
	}

	protected void log(String msg, Element element) {
		messager.printMessage(Diagnostic.Kind.NOTE, msg, element);
	}

	protected boolean hasBeenProcessed(Element typeElement) {
		return doneProcessing.contains(typeElement);
	}

	protected void markAsProcessed(Element typeElement) {
		doneProcessing.add(typeElement);
	}

}
