package com.github.thorbenkuck.di.processor;

import com.github.thorbenkuck.di.processor.exceptions.ProcessingException;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.processing.Messager;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.*;

public class Logger {

	private static Messager messager;
	private static final ThreadLocal<Element> localRootElement = new ThreadLocal<>();
	private static final ThreadLocal<Class<? extends Annotation>> localCurrentAnnotation = new ThreadLocal<>();
	private static final boolean logToSystemOut = ProcessorProperties.isEnabled(PropertyKeys.LOG_TO_SYSTEM_OUT);
	private static final String LOGGING_PATTERN = "[%5.5s] [%12.12s] [%10.10s] [%20.20s]: ";
	private static final List<LogBuffer> logBuffer = new ArrayList<>();

	private static void writeLogMessages(
			@Nullable Element targetElement,
			@Nullable AnnotationMirror annotationMirror,
			@Nullable AnnotationValue annotationValue,
			@NotNull Level logLevel,
			@NotNull String messageTemplate,
			@NotNull Object[] argsForMessage
	) {
		Element rootElement = localRootElement.get();
		String simpleMessage = createMessagerMessage(messageTemplate, argsForMessage);

		logLevel.getDiagnosticKind().forEach(kind -> {
			messager.printMessage(kind, simpleMessage, targetElement, annotationMirror, annotationValue);
		});

		if (rootElement != null && targetElement != null && !rootElement.equals(targetElement)) {
			String rootElementWarning = "[" + logLevel.name() + "@" + targetElement.getSimpleName() + "]: " + simpleMessage;
			messager.printMessage(Diagnostic.Kind.NOTE, rootElementWarning, rootElement, annotationMirror, annotationValue);
		}

		if (logToSystemOut) {
			Element element = Optional.ofNullable(targetElement).orElse(rootElement);
			String finalMessage = createFullMessage(element, logLevel, messageTemplate, argsForMessage);
			System.out.println(finalMessage);
		}
	}

	public static void log(
			@Nullable Element targetElement,
			@Nullable AnnotationMirror annotationMirror,
			@Nullable AnnotationValue annotationValue,
			@NotNull Level logLevel,
			@NotNull String messageTemplate,
			@NotNull Object... argsForMessage
	) {
		if (logLevel.isDisabled()) {
			return;
		}
		if (messager == null) {
			synchronized (logBuffer) {
				logBuffer.add(new LogBuffer(targetElement, annotationMirror, annotationValue, logLevel, messageTemplate, argsForMessage));
			}
			return;
		}

		synchronized (logBuffer) {
			if(!logBuffer.isEmpty()) {
				logBuffer.forEach(entry -> writeLogMessages(
						entry.targetElement,
						entry.annotationMirror,
						entry.annotationValue,
						entry.logLevel,
						entry.messageTemplate,
						entry.argsForMessage
				));
				logBuffer.clear();
			}
		}
		writeLogMessages(targetElement, annotationMirror, annotationValue, logLevel, messageTemplate, argsForMessage);
	}

	public static void log(
			@Nullable Element targetElement,
			@Nullable AnnotationMirror annotationMirror,
			@NotNull Level logLevel,
			@NotNull String messageTemplate,
			Object... argsForMessage
	) {
		if (logLevel.isDisabled()) {
			return;
		}
		log(targetElement, annotationMirror, null, logLevel, messageTemplate, argsForMessage);
	}

	public static void log(
			@Nullable Element targetElement,
			@NotNull Level logLevel,
			@NotNull String messageTemplate,
			Object... argsForMessage
	) {
		if (logLevel.isDisabled()) {
			return;
		}
		log(targetElement, null, logLevel, messageTemplate, argsForMessage);
	}

	public static void log(
			@NotNull Level logLevel,
			@NotNull String messageTemplate,
			Object... argsForMessage
	) {
		if (logLevel.isDisabled()) {
			return;
		}
		log(null, logLevel, messageTemplate, argsForMessage);
	}

	public static void catching(Throwable throwable) {
		if (throwable instanceof ProcessingException) {
			catching((ProcessingException) throwable);
		} else {
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			throwable.printStackTrace(printWriter);

			error("Encountered unexpected Exception: %s", stringWriter.toString());
			if (logToSystemOut) {
				throwable.printStackTrace();
			}
		}
	}

	public static void catching(ProcessingException processingException) {
		error(processingException.getElement(), processingException.getMessage());

		if (logToSystemOut) {
			processingException.printStackTrace();
		}
	}

	public static void error(AnnotationMirror mirror, Element element, String msg) {
		log(element, mirror, Level.ERROR, msg);
	}

	public static void error(Element element, String msg, Object... args) {
		log(element, Level.ERROR, msg, args);
	}

	public static void error(String msg, Object... args) {
		log(Level.ERROR, msg, args);
	}

	public static void warn(String msg, Object... args) {
		log(Level.WARN, msg, args);
	}

	public static void warn(Element element, String msg, Object... args) {
		log(element, Level.WARN, msg, args);
	}

	public static void reflectionWarning(ExecutableElement element) {
		if (ProcessorProperties.isEnabled(PropertyKeys.WARN_REFLECTION_USAGE)) {
			warn(element, "This method requires the use of reflection, which is highly discouraged. Consider making it protected, package private or even public to reduce runtime reflection overhead.");
		}
	}

	public static void reflectionWarning(VariableElement element) {
		if (ProcessorProperties.isEnabled(PropertyKeys.WARN_REFLECTION_USAGE)) {
			warn(element, "This variable requires the use of reflection, which is highly discouraged. Consider making it protected, package private or even public to reduce runtime reflection overhead.");
		}
	}

	public static void info(@Nullable Element element, @NotNull String msg, @NotNull Object... args) {
		log(element, Level.INFO, msg, args);
	}

	public static void info(@NotNull String msg, @NotNull Object... args) {
		info(null, msg, args);
	}

	public static void info(@NotNull String msg) {
		info(null, msg, new Object[]{});
	}

	public static void debug(@Nullable Element element, @NotNull String msg, @NotNull Object... args) {
		log(element, Level.DEBUG, msg, args);
	}

	public static void debug(@NotNull String msg, @NotNull Object... args) {
		debug(null, msg, args);
	}

	public static boolean useSystemOut() {
		return logToSystemOut;
	}

	static void setThreadState(Class<? extends Annotation> currentAnnotation, Element rootElement) {
		localRootElement.set(rootElement);
		localCurrentAnnotation.set(currentAnnotation);
	}

	public static void clearThreadState() {
		localCurrentAnnotation.remove();
		localRootElement.remove();
	}

	public static synchronized void setMessager(Messager messager) {
		Logger.messager = messager;
		if (ProcessorProperties.isEnabled(PropertyKeys.DEBUG_ENABLED)) {
			debug("Debug enabled");
		}
	}

	public enum Level {
		INFO {
			@Override
			List<Diagnostic.Kind> getDiagnosticKind() {
				return Collections.singletonList(Diagnostic.Kind.NOTE);
			}
		},
		DEBUG {
			@Override
			List<Diagnostic.Kind> getDiagnosticKind() {
				return Collections.singletonList(Diagnostic.Kind.OTHER);
			}

			@Override
			boolean isEnabled() {
				return ProcessorProperties.isEnabled(PropertyKeys.DEBUG_ENABLED);
			}
		},
		WARN {
			@Override
			List<Diagnostic.Kind> getDiagnosticKind() {
				return Arrays.asList(Diagnostic.Kind.WARNING, Diagnostic.Kind.MANDATORY_WARNING);
			}
		},
		ERROR {
			@Override
			List<Diagnostic.Kind> getDiagnosticKind() {
				return Collections.singletonList(Diagnostic.Kind.ERROR);
			}
		};

		abstract List<Diagnostic.Kind> getDiagnosticKind();

		boolean isEnabled() {
			return true;
		}

		boolean isDisabled() { return !isEnabled(); }
	}

	private static String createFullMessage(
			@Nullable Element targetElement,
			@NotNull Level logLevel,
			@NotNull String messageTemplate,
			@NotNull Object... argsForMessage
	) {
		Class<? extends Annotation> annotationType = localCurrentAnnotation.get();
		String originName = targetElement == null ? "" : targetElement.getKind().name() + " " + targetElement.getSimpleName();
		String annotationName = annotationType == null ? "" : annotationType.getSimpleName();

		String prefix = String.format(LOGGING_PATTERN, logLevel, Thread.currentThread().getName(), annotationName, originName);
		String suffix = createMessagerMessage(messageTemplate, argsForMessage);

		return prefix + suffix;
	}

	private static String createMessagerMessage(
			String messageTemplate,
			Object... args
	) {
		if (args.length == 0) {
			return messageTemplate;
		} else {
			return String.format(messageTemplate, args);
		}
	}

	private static class LogBuffer {
		private final Element targetElement;
		private final AnnotationMirror annotationMirror;
		private final AnnotationValue annotationValue;
		private final Level logLevel;
		private final String messageTemplate;
		private final Object[] argsForMessage;

		private LogBuffer(Element targetElement, AnnotationMirror annotationMirror, AnnotationValue annotationValue, Level logLevel, String messageTemplate, Object[] argsForMessage) {
			this.targetElement = targetElement;
			this.annotationMirror = annotationMirror;
			this.annotationValue = annotationValue;
			this.logLevel = logLevel;
			this.messageTemplate = messageTemplate;
			this.argsForMessage = argsForMessage;
		}
	}
}
