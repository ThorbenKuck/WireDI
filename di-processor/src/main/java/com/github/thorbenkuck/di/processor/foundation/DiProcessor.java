package com.github.thorbenkuck.di.processor.foundation;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.Generated;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.time.LocalDateTime;
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
	protected Logger logger;

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
		logger = new Logger(processingEnv.getMessager());
	}

	@Override
	public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		int i = 0;
		logger.setUseSystemOut(true);
		for(Class<? extends Annotation> type : supportedAnnotations()) {
			for(Element element : roundEnv.getElementsAnnotatedWith(type)) {
				logger.setRootElement(element);
				logger.setCurrentAnnotation(type);
				if(!hasBeenProcessed(element)) {
					try {
						logger.log("[" + i++ + "] Attempting to process for annotation " + type.getName());
						handle(element);
						logger.log("[" + i + "] Finished Successfully");
					} catch (ProcessingException e) {
						logger.error(e.getMessage(), e.getElement());
					} catch (Exception e) {
						logger.error("[" + i + "] Encountered an unexpected Exception " + e);
					}
				}
			}
		}
		return true;
	}

	protected void appendGeneratedAnnotation(TypeSpec.Builder builder, String comment) {
		builder.addAnnotation(AnnotationSpec.builder(Generated.class)
				.addMember("value", "$S", getClass().getName())
				.addMember("date", "$S", LocalDateTime.now().toString())
				.addMember("comment", "$S", comment)
				.build());
	}

	protected abstract void handle(Element element);

	protected boolean hasBeenProcessed(Element typeElement) {
		return doneProcessing.contains(typeElement);
	}

	protected void markAsProcessed(Element typeElement) {
		doneProcessing.add(typeElement);
	}

}