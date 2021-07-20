package com.github.thorbenkuck.di.processor.foundation;

import com.github.thorbenkuck.di.processor.ClassWriter;
import com.github.thorbenkuck.di.processor.FieldInjector;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.Generated;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public abstract class DiProcessor extends AbstractProcessor {

	private final List<Element> doneProcessing = new ArrayList<>();
	protected Types types;
	protected Elements elements;
	protected Filer filer;
	protected Logger logger;

	protected abstract Collection<Class<? extends Annotation>> supportedAnnotations();

	protected void markAsGenerated(TypeSpec.Builder builder, String... comments) {
		AnnotationSpec.Builder annotationBuilder = AnnotationSpec.builder(Generated.class)
				.addMember("value", "$S", getClass().getName())
				.addMember("date", "$S", LocalDateTime.now().toString());
		if (comments.length > 0) {
			annotationBuilder.addMember("comments", "$S", String.join("\n", comments));
		}
		builder.addAnnotation(annotationBuilder.build());
	}

	protected abstract void handle(Element element);

	protected boolean hasBeenProcessed(Element typeElement) {
		return doneProcessing.contains(typeElement);
	}

	protected void markAsProcessed(Element typeElement) {
		doneProcessing.add(typeElement);
	}

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

		ClassWriter.filer = filer;
		ClassWriter.elements = elements;
		FieldInjector.logger = logger;
	}

	private Set<? extends Element> findAllAnnotatedClasses(TypeElement annotation, RoundEnvironment roundEnvironment) {
		return roundEnvironment.getElementsAnnotatedWith(annotation);
	}

	private Set<? extends Element> analyzeInclusive(Set<? extends Element> foundElements, RoundEnvironment roundEnvironment) {
		Set<Element> result = new HashSet<>();
		for(Element element : foundElements) {
			if(element.getKind() == ElementKind.ANNOTATION_TYPE) {
				logger.log("Found a meta annotation!");
				Set<? extends Element> meta = findAllAnnotatedClasses((TypeElement) element, roundEnvironment);
				Collection<? extends Element> elements = analyzeInclusive(meta, roundEnvironment);
				result.addAll(elements);
			} else {
				result.add(element);
			}
		}

		return result;
	}

	@Override
	public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		int i = 0;
		logger.setUseSystemOut(true);
		for (Class<? extends Annotation> type : supportedAnnotations()) {
			Set<? extends Element> root = roundEnv.getElementsAnnotatedWith(type);
			Set<? extends Element> toProcess = analyzeInclusive(root, roundEnv);

			for (Element element : toProcess) {
				logger.setRootElement(element);
				logger.setCurrentAnnotation(type);
				if (!hasBeenProcessed(element)) {
					try {
						logger.log("[" + i++ + "] Attempting to process the annotation " + type.getName());
						handle(element);
						logger.log("[" + i + "] Finished Successfully");
					} catch (ProcessingException e) {
						logger.error(e.getMessage(), e.getElement());
					} catch (Exception e) {
						logger.error("[" + i + "] Encountered an unexpected Exception " + e);
						logger.catching(e);
					}
				}
			}
		}
		return true;
	}

}
