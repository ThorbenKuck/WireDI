package com.wiredi.compiler.logger;
import com.wiredi.compiler.errors.ProcessingException;
import com.wiredi.compiler.logger.dispatcher.LogDispatcher;
import com.wiredi.compiler.logger.dispatcher.WorkerThreadLogDispatcher;
import com.wiredi.compiler.logger.writer.MessagerLogWriter;
import com.wiredi.compiler.logger.writer.SystemOutLogWriter;
import com.wiredi.lang.SingletonSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Supplier;

public class Logger {

	private static final ThreadLocal<Element> localRootElement = new ThreadLocal<>();
	private static final ThreadLocal<Class<? extends Annotation>> localCurrentAnnotation = new ThreadLocal<>();
	private static final Supplier<LogDispatcher> DEFAULT_DISPATCHER = () -> new WorkerThreadLogDispatcher(
			new SystemOutLogWriter(LogPattern.DEFAULT),
			new MessagerLogWriter()
	);
	private static final LoggerCache loggerCache = new LoggerCache();

	private final LogDispatcher logDispatcher;
	private final Class<?> type;
	private final LoggerProperties properties;

	public static void trackRootElement(Element element, Runnable scope) {
		var previousElement = localRootElement.get();
		localRootElement.set(element);
		try {
			scope.run();
		} finally {
			localRootElement.set(previousElement);
		}
	}

	public static void trackAnnotation(Class<? extends Annotation> annotationType, Runnable scope) {
		var previousElement = localCurrentAnnotation.get();
		localCurrentAnnotation.set(annotationType);
		try {
			scope.run();
		} finally {
			localCurrentAnnotation.set(previousElement);
		}
	}

	public static Logger get(Class<?> type) {
		return loggerCache.getOrSet(type, () -> new Logger(type));
	}

	public Logger(Class<?> type, LoggerProperties loggerProperties, LogDispatcher logDispatcher) {
		this.type = type;
		this.properties = loggerProperties;
		this.logDispatcher = logDispatcher;
	}

	public Logger(Class<?> type, LoggerProperties loggerProperties) {
		this(type, loggerProperties, DEFAULT_DISPATCHER.get());
	}

	public Logger(Class<?> type) {
		this(type, new LoggerProperties());
	}

	public void log(
			@Nullable Element targetElement,
			@Nullable AnnotationMirror annotationMirror,
			@Nullable AnnotationValue annotationValue,
			@NotNull LogLevel logLevel,
			@NotNull Supplier<String> messageSupplier
	) {
		if (logLevel.isDisabled(properties.logLevel())) {
			return;
		}
		LogEntry logEntry = new LogEntry(
				targetElement,
				localRootElement.get(),
				localCurrentAnnotation.get(),
				annotationMirror,
				annotationValue,
				type,
				logLevel,
				messageSupplier.get()
		);

		logDispatcher.dispatch(logEntry);
	}

	public void log(
			@Nullable Element targetElement,
			@Nullable AnnotationMirror annotationMirror,
			@NotNull LogLevel logLevel,
			@NotNull Supplier<String> message
	) {
		if (logLevel.isDisabled(properties.logLevel())) {
			return;
		}
		log(targetElement, annotationMirror, null, logLevel, message);
	}

	public void log(
			@Nullable Element targetElement,
			@Nullable AnnotationMirror annotationMirror,
			@NotNull LogLevel logLevel,
			@NotNull String message
	) {
		if (logLevel.isDisabled(properties.logLevel())) {
			return;
		}
		log(targetElement, annotationMirror, null, logLevel, new SingletonSupplier<>(message));
	}

	public void log(
			@Nullable Element targetElement,
			@NotNull LogLevel logLevel,
			@NotNull Supplier<String> message
	) {
		if (logLevel.isDisabled(properties.logLevel())) {
			return;
		}
		log(targetElement, null, logLevel, message);
	}

	public void log(
			@Nullable Element targetElement,
			@NotNull LogLevel logLevel,
			@NotNull String message
	) {
		if (logLevel.isDisabled(properties.logLevel())) {
			return;
		}
		log(targetElement, null, logLevel, new SingletonSupplier<>(message));
	}

	public void log(
			@NotNull LogLevel logLevel,
			@NotNull String message
	) {
		if (logLevel.isDisabled(properties.logLevel())) {
			return;
		}
		log(null, logLevel, new SingletonSupplier<>(message));
	}

	public void log(
			@NotNull LogLevel logLevel,
			@NotNull Supplier<String> message
	) {
		if (logLevel.isDisabled(properties.logLevel())) {
			return;
		}
		log(null, logLevel, message);
	}

	public void catching(Throwable throwable) {
		if (throwable instanceof ProcessingException) {
			catching((ProcessingException) throwable);
		} else {
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			throwable.printStackTrace(printWriter);

			error(() -> "Encountered unexpected Exception: " + stringWriter);
			if (properties.logToSystemOut()) {
				throwable.printStackTrace();
			}
		}
	}

	public void catching(ProcessingException processingException) {
		error(processingException.getElement(), processingException.getMessage());

		if (properties.logToSystemOut()) {
			processingException.printStackTrace();
		}
	}

	public void error(AnnotationMirror mirror, Element element, String msg) {
		log(element, mirror, LogLevel.ERROR, msg);
	}

	public void error(AnnotationMirror mirror, Element element, Supplier<String> msg) {
		log(element, mirror, LogLevel.ERROR, msg);
	}

	public void error(Element element, String msg) {
		log(element, LogLevel.ERROR, msg);
	}

	public void error(Element element, Supplier<String> msg) {
		log(element, LogLevel.ERROR, msg);
	}

	public void error(String msg) {
		log(LogLevel.ERROR, msg);
	}

	public void error(Supplier<String> msg) {
		log(LogLevel.ERROR, msg);
	}

	public void warn(Element element, String msg) {
		log(element, LogLevel.WARN, msg);
	}

	public void warn(Element element, Supplier<String> msg) {
		log(element, LogLevel.WARN, msg);
	}

	public void warn(String msg) {
		log(LogLevel.WARN, msg);
	}

	public void warn(Supplier<String> msg) {
		log(LogLevel.WARN, msg);
	}

	public void reflectionWarning(ExecutableElement element) {
		if (properties.warnReflectionUsage()) {
			warn(element, "This method requires the use of reflection, which is highly discouraged. Consider making it protected, package private or even public to reduce runtime reflection overhead.");
		}
	}

	public void reflectionWarning(VariableElement element) {
		if (properties.warnReflectionUsage()) {
			warn(element, "This variable requires the use of reflection, which is highly discouraged. Consider making it protected, package private or even public to reduce runtime reflection overhead.");
		}
	}

	public void info(@Nullable Element element, @NotNull String msg) {
		log(element, LogLevel.INFO, msg);
	}

	public void info(@Nullable Element element, @NotNull Supplier<String> msg) {
		log(element, LogLevel.INFO, msg);
	}

	public void info(@NotNull String msg) {
		info(null, msg);
	}

	public void info(@NotNull Supplier<String> msg) {
		info(null, msg);
	}

	public void debug(@Nullable Element element, @NotNull String msg) {
		log(element, LogLevel.DEBUG, msg);
	}

	public void debug(@Nullable Element element, @NotNull Supplier<String> msg) {
		log(element, LogLevel.DEBUG, msg);
	}

	public void debug(@NotNull String msg) {
		debug(null, msg);
	}

	public void debug(@NotNull Supplier<String> msg) {
		debug(null, msg);
	}

	public void trace(@Nullable Element element, @NotNull String msg) {
		log(element, LogLevel.TRACE, msg);
	}

	public void trace(@Nullable Element element, @NotNull Supplier<String> msg) {
		log(element, LogLevel.TRACE, msg);
	}

	public void trace(@NotNull String msg) {
		trace(null, msg);
	}

	public void trace(@NotNull Supplier<String> msg) {
		trace(null, msg);
	}

	public boolean isEnabled(LogLevel logLevel) {
		return this.properties.logLevel().isEnabled(logLevel);
	}

	public boolean isDisabled(LogLevel logLevel) {
		return !isEnabled(logLevel);
	}

	@Override
	public String toString() {
		return "Logger(" + type + ")";
	}
}
