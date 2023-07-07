package com.wiredi.compiler.logger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;

public record LogEntry(
		@Nullable Element targetElement,
		@Nullable Element rootElement,
		@Nullable Class<? extends Annotation> annotationType,
		@Nullable AnnotationMirror annotationMirror,
		@Nullable AnnotationValue annotationValue,
		@NotNull Class<?> loggerType,
		@NotNull LogLevel logLevel,
		@NotNull String message
) {
}
