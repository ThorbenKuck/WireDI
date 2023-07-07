package com.wiredi.processor;

import com.wiredi.processor.lang.AnnotationProcessorResource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class AspectIgnoredAnnotations {

	private final List<Predicate<String>> ignoredPredicates = new ArrayList<>();
	private final Elements elements;
	private final Types types;
	private final Filer filer;

	public AspectIgnoredAnnotations(Elements elements, Types types, Filer filer) {
		this.elements = elements;
		this.types = types;
		this.filer = filer;
	}

	private static final String FILE_NAME = "aop-ignored.types";

	private static final List<Predicate<String>> DEFAULT_IGNORED_ANNOTATIONS = Arrays.asList(
			Pattern.compile(Pattern.quote(Override.class.getName())).asMatchPredicate(),
			Pattern.compile(Pattern.quote(PostConstruct.class.getName())).asMatchPredicate(),
			Pattern.compile(Pattern.quote(PreDestroy.class.getName())).asMatchPredicate(),
			Pattern.compile(Pattern.quote(Singleton.class.getName())).asMatchPredicate(),
			Pattern.compile(Pattern.quote(Inject.class.getName())).asMatchPredicate()
	);

	@PostConstruct
	protected void postConstruct() {
		AnnotationProcessorResource resource = new AnnotationProcessorResource(filer, FILE_NAME);

		if (resource.doesNotExist()) {
			this.ignoredPredicates.addAll(DEFAULT_IGNORED_ANNOTATIONS);
			return;
		}

		try (BufferedReader reader = new BufferedReader(resource.openReader())) {
			while (reader.ready()) {
				String line = reader.readLine();
				if (!line.trim().startsWith("p:")) {
					line = Pattern.quote(line);
				} else {
					line = line.substring("p:".length());
				}

				ignoredPredicates.add(Pattern.compile(line).asMatchPredicate());
			}
		} catch (IOException e) {
			this.ignoredPredicates.addAll(DEFAULT_IGNORED_ANNOTATIONS);
		} catch (Exception e) {
			e.printStackTrace();
			this.ignoredPredicates.addAll(DEFAULT_IGNORED_ANNOTATIONS);
		}
	}

	public boolean isIgnored(TypeMirror typeMirror) {
		Element element = types.asElement(typeMirror);
		return isIgnored(element);
	}

	public boolean isIgnored(Class<? extends Annotation> type) {
		return isIgnored(type.getName());
	}

	public boolean isIgnored(Element element) {
		if (element.getKind() != ElementKind.ANNOTATION_TYPE) {
			return false;
		}


		return isIgnored(((TypeElement) element).getQualifiedName().toString());
	}

	private boolean isIgnored(String name) {
		for (Predicate<String> className : ignoredPredicates) {
			if (className.test(name)) {
				return true;
			}
		}
		return false;
	}
}
