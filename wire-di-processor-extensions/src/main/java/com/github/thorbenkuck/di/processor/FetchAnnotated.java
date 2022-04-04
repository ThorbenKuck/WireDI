package com.github.thorbenkuck.di.processor;

import javax.lang.model.element.*;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class FetchAnnotated {

	public static List<VariableElement> fields(TypeElement typeElement, Class<? extends Annotation> annotation) {
		final List<VariableElement> variableElements = new ArrayList<>();
		for (Element enclosedElement : typeElement.getEnclosedElements()) {
			if (enclosedElement.getKind() == ElementKind.FIELD && enclosedElement.getAnnotation(annotation) != null) {
				variableElements.add((VariableElement) enclosedElement);
			}
		}

		return variableElements;
	}

	public static List<VariableElement> parameters(ExecutableElement executableElement, Class<? extends Annotation> annotation) {
		final List<VariableElement> variableElements = new ArrayList<>();
		for (Element enclosedElement : executableElement.getParameters()) {
			if (enclosedElement.getKind() == ElementKind.PARAMETER && enclosedElement.getAnnotation(annotation) != null) {
				variableElements.add((VariableElement) enclosedElement);
			}
		}

		return variableElements;
	}

	public static List<ExecutableElement> methods(TypeElement typeElement, Class<? extends Annotation> annotation) {
		final List<ExecutableElement> variableElements = new ArrayList<>();
		for (Element enclosedElement : typeElement.getEnclosedElements()) {
			if (enclosedElement.getKind() == ElementKind.METHOD && enclosedElement.getAnnotation(annotation) != null) {
				variableElements.add((ExecutableElement) enclosedElement);
			}
		}

		return variableElements;
	}

	public static List<ExecutableElement> constructors(TypeElement typeElement, Class<? extends Annotation> annotation) {
		final List<ExecutableElement> variableElements = new ArrayList<>();
		for (Element enclosedElement : typeElement.getEnclosedElements()) {
			if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR && enclosedElement.getAnnotation(annotation) != null) {
				variableElements.add((ExecutableElement) enclosedElement);
			}
		}

		return variableElements;
	}

}
