package com.github.thorbenkuck.di.processor.foundation;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;

public class Logger {

    private static Messager messager;
    private static Element rootElement;
    private static Class<? extends Annotation> currentAnnotation;
    private static boolean alsoUseSystemOut = true;

    public static void setMessager(Messager messager) {
        Logger.messager = messager;
    }

    public static void error(String msg, Element element, AnnotationMirror mirror) {
        messager.printMessage(Diagnostic.Kind.ERROR, msg, element, mirror);
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

    public static void log(String msg, Element element) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg, element);
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

    public static void log(String msg) {
        log(msg, null);
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
        if (rootElement != null && !rootElement.equals(element)) {
            messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, msg, rootElement);
            messager.printMessage(Diagnostic.Kind.WARNING, msg, rootElement);
        }
        if (useSystemOut()) {
            System.out.println("[WARNING] " + msg + " " + element);
        }
    }

    static void setCurrentAnnotation(Class<? extends Annotation> currentAnnotation) {
        Logger.currentAnnotation = currentAnnotation;
    }

    static void setRootElement(Element rootElement) {
        Logger.rootElement = rootElement;
    }
}
