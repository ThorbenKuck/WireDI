package com.github.thorbenkuck.di.processor.foundation;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;

public class Logger {

	private final Messager messager;
	private Element rootElement;
	private Class<? extends Annotation> currentAnnotation;
	private boolean alsoUseSystemOut = true;

	Logger(Messager messager) {
		this.messager = messager;
	}

	public void error(String msg, Element element, AnnotationMirror mirror) {
		messager.printMessage(Diagnostic.Kind.ERROR, msg, element, mirror);
		if(rootElement != null && !rootElement.equals(element)) {
			messager.printMessage(Diagnostic.Kind.ERROR, msg, rootElement, mirror);
		}
		if(useSystemOut()) {
			System.err.println("[ERROR] " + msg + " " + element);
		}
	}

	public void error(String msg, Element element) {
		error(msg, element, null);
	}

	public void catching(Throwable throwable) {
		error("While processing annotation " + currentAnnotation + " on element " + rootElement.getSimpleName() + ": Encountered the Exception " + throwable);

		if(useSystemOut()) {
			throwable.printStackTrace();
		}
	}

	public void error(String msg) {
		error(msg, null);
	}

	public void log(String msg, Element element) {
		messager.printMessage(Diagnostic.Kind.NOTE, msg, element);
		if(rootElement != null && !rootElement.equals(element)) {
			messager.printMessage(Diagnostic.Kind.NOTE, msg, rootElement);
		}
		if(useSystemOut()) {
			String toLog = "[INFO] " + msg;
			if(element != null) {
				toLog += " " + element;
			}
			System.out.println(toLog);
		}
	}

	public void log(String msg) {
		log(msg, null);
	}

	public boolean useSystemOut() {
		return alsoUseSystemOut;
	}

	public void setUseSystemOut(boolean alsoUseSystemOut) {
		this.alsoUseSystemOut = alsoUseSystemOut;
	}

	public void warn(String msg, Element element) {
		messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, msg, element);
		messager.printMessage(Diagnostic.Kind.WARNING, msg, element);
		if(rootElement != null && !rootElement.equals(element)) {
			messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, msg, rootElement);
			messager.printMessage(Diagnostic.Kind.WARNING, msg, rootElement);
		}
		if(useSystemOut()) {
			System.out.println("[WARN] " + msg + " " + element);
		}
	}

	void setCurrentAnnotation(Class<? extends Annotation> currentAnnotation) {
		this.currentAnnotation = currentAnnotation;
	}

	void setRootElement(Element rootElement) {
		this.rootElement = rootElement;
	}
}
