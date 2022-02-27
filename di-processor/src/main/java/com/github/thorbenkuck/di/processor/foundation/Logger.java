package com.github.thorbenkuck.di.processor.foundation;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;

public class Logger {

    private static Messager messager;
    private static ThreadLocal<Element> localRootElement = new ThreadLocal<>();
    private static ThreadLocal<Class<? extends Annotation>> localCurrentAnnotation = new ThreadLocal<>();
    private static boolean alsoUseSystemOut = true;

    public static void setMessager(Messager messager) {
        Logger.messager = messager;
    }

    public static void error(String msg, Element element, AnnotationMirror mirror) {
        messager.printMessage(Diagnostic.Kind.ERROR, msg, element, mirror);
        Element rootElement = localRootElement.get();
        if (rootElement != null && !rootElement.equals(element)) {
            messager.printMessage(Diagnostic.Kind.ERROR, msg, rootElement, mirror);
        }
        if (useSystemOut()) {
            System.err.println("[ERROR] " + msg + " " + element);
        }
    }

    public static void error(String msg, Element element) {
        error(msg, element, null);
    }

    public static void catching(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);

        error(stringWriter.toString());

		if (useSystemOut()) {
			throwable.printStackTrace();
		}
    }

    public static void error(String msg) {
        error(msg, null);
    }

    public static void info(String msg, Element element) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg, element);
        Element rootElement = localRootElement.get();
        if (rootElement != null && !rootElement.equals(element)) {
            messager.printMessage(Diagnostic.Kind.NOTE, msg, rootElement);
        }
        if (useSystemOut()) {
            String toLog = "[INFO] " + msg;
            if (element != null) {
                toLog += " " + element;
            }
            System.out.println(toLog);
        }
    }

    public static void info(String msg) {
        info(msg, null);
    }

    public static boolean useSystemOut() {
        return alsoUseSystemOut;
    }

    public static void setUseSystemOut(boolean alsoUseSystemOut) {
        Logger.alsoUseSystemOut = alsoUseSystemOut;
    }

    public static void warn(String msg, Element element) {
        messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, msg, element);
        messager.printMessage(Diagnostic.Kind.WARNING, msg, element);
        Element rootElement = localRootElement.get();
        if (rootElement != null && !rootElement.equals(element)) {
            messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, msg, rootElement);
            messager.printMessage(Diagnostic.Kind.WARNING, msg, rootElement);
        }
        if (useSystemOut()) {
            System.out.println("[WARNING] " + msg + " " + element);
        }
    }

    public static void reflectionWarning(ExecutableElement element) {
        warn("This method requires the use of reflection, which is highly discouraged. Consider making it protected, package private or even public to reduce runtime reflection overhead.", element);
    }

    public static void reflectionWarning(VariableElement element) {
        warn("This variable requires the use of reflection, which is highly discouraged. Consider making it protected, package private or even public to reduce runtime reflection overhead.", element);
    }

    static void setCurrentAnnotation(Class<? extends Annotation> currentAnnotation) {
        Logger.localCurrentAnnotation.set(currentAnnotation);
    }

    static void setRootElement(Element rootElement) {
        Logger.localRootElement.set(rootElement);
    }
}
