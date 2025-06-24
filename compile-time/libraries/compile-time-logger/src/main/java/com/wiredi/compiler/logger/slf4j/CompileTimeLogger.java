package com.wiredi.compiler.logger.slf4j;

import com.wiredi.compiler.logger.LogPattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.event.Level;

import javax.annotation.processing.Messager;
import javax.lang.model.element.*;
import java.util.function.Supplier;

public class CompileTimeLogger extends MessagerLogger {

    public CompileTimeLogger(
            @NotNull String name,
            @NotNull Level level,
            @NotNull LogPattern logPattern,
            boolean logToConsole,
            @Nullable Messager messager
    ) {
        super(name, level, logPattern, logToConsole, messager);
    }

    public static CompileTimeLogger getLogger(Class<?> type) {
        return CompileTimeLoggerFactory.getLogger(type);
    }

    public static CompileTimeLogger getLogger(String name) {
        return CompileTimeLoggerFactory.getInstance().getLogger(name);
    }

    public void log(
            @NotNull Level logLevel,
            @Nullable Element targetElement,
            @Nullable AnnotationMirror annotationMirror,
            @Nullable AnnotationValue annotationValue,
            @Nullable Throwable throwable,
            @NotNull Supplier<String> message
    ) {
        if (isEnabledForLevel(logLevel)) {
            MessagerContext.runNested(instance -> {
                if (targetElement != null) instance.setElement(targetElement);
                if (annotationMirror != null) instance.setAnnotationMirror(annotationMirror);
                if (annotationValue != null) instance.setAnnotationValue(annotationValue);

                if (throwable != null) {
                    doLog(logLevel, message.get(), throwable);
                } else {
                    doLog(logLevel, message.get());
                }
            });
        }
    }

    public void log(
            @NotNull Level logLevel,
            @Nullable Element targetElement,
            @Nullable AnnotationMirror annotationMirror,
            @Nullable AnnotationValue annotationValue,
            @Nullable Throwable throwable,
            @NotNull String message
    ) {
        if (isEnabledForLevel(logLevel)) {
            MessagerContext.runNested(instance -> {
                if (targetElement != null) instance.setElement(targetElement);
                if (annotationMirror != null) instance.setAnnotationMirror(annotationMirror);
                if (annotationValue != null) instance.setAnnotationValue(annotationValue);

                if (throwable != null) {
                    doLog(logLevel, message, throwable);
                } else {
                    doLog(logLevel, message);
                }
            });
        }
    }

    public void error(AnnotationMirror mirror, Element element, Throwable throwable, String msg) {
        log(Level.ERROR, element, mirror, null, throwable, msg);
    }

    public void error(AnnotationMirror mirror, Element element, Throwable throwable, Supplier<String> msg) {
        log(Level.ERROR, element, mirror, null, throwable, msg);
    }

    public void error(AnnotationMirror mirror, Element element, Supplier<String> msg) {
        log(Level.ERROR, element, mirror, null, null, msg);
    }

    public void error(Element element, String msg) {
        log(Level.ERROR, element, null, null, null, msg);
    }

    public void error(Element element, Supplier<String> msg) {
        log(Level.ERROR, element, null, null, null, msg);
    }

    public void error(Element element, Throwable throwable, String msg) {
        log(Level.ERROR, element, null, null, throwable, msg);
    }

    public void error(Element element, Throwable throwable, Supplier<String> msg) {
        log(Level.ERROR, element, null, null, throwable, msg);
    }

    public void error(String msg) {
        log(Level.ERROR, null, null, null, null, msg);
    }

    public void error(Supplier<String> msg) {
        log(Level.ERROR, null, null, null, null, msg);
    }

    public void error(String msg, Throwable throwable) {
        log(Level.ERROR, null, null, null, throwable, msg);
    }

    public void error(Supplier<String> msg, Throwable throwable) {
        log(Level.ERROR, null, null, null, throwable, msg);
    }

    public void warn(Element element, String msg) {
        log(Level.WARN, element, null, null, null, msg);
    }

    public void warn(Element element, Supplier<String> msg) {
        log(Level.ERROR, element, null, null, null, msg);
    }

    public void warn(String msg) {
        log(Level.WARN, null, null, null, null, msg);
    }

    public void warn(Supplier<String> msg) {
        log(Level.WARN, null, null, null, null, msg);
    }

    public void reflectionWarning(ExecutableElement element) {
        warn(element, "This method requires the use of reflection, which is highly discouraged. Consider making it protected, package private or even public to reduce runtime reflection overhead.");
    }

    public void reflectionWarning(VariableElement element) {
        warn(element, "This variable requires the use of reflection, which is highly discouraged. Consider making it protected, package private or even public to reduce runtime reflection overhead.");
    }

    public void info(@Nullable Element element, @NotNull String msg) {
        log(Level.INFO, element, null, null, null, msg);
    }

    public void info(@Nullable Element element, @NotNull Supplier<String> msg) {
        log(Level.INFO, element, null, null, null, msg);
    }

    public void info(@NotNull String msg) {
        log(Level.INFO, null, null, null, null, msg);
    }

    public void info(@NotNull Supplier<String> msg) {
        log(Level.INFO, null, null, null, null, msg);
    }

    public void debug(@Nullable Element element, @NotNull String msg) {
        log(Level.DEBUG, element, null, null, null, msg);
    }

    public void debug(@Nullable Element element, @NotNull Supplier<String> msg) {
        log(Level.DEBUG, element, null, null, null, msg);
    }

    public void debug(@NotNull String msg) {
        log(Level.DEBUG, null, null, null, null, msg);
    }

    public void debug(@NotNull Supplier<String> msg) {
        log(Level.DEBUG, null, null, null, null, msg);
    }

    public void trace(@Nullable Element element, @NotNull String msg) {
        log(Level.TRACE, element, null, null, null, msg);
    }

    public void trace(@Nullable Element element, @NotNull Supplier<String> msg) {
        log(Level.TRACE, element, null, null, null, msg);
    }

    public void trace(@NotNull String msg) {
        log(Level.TRACE, null, null, null, null, msg);
    }

    public void trace(@NotNull Supplier<String> msg) {
        log(Level.TRACE, null, null, null, null, msg);
    }

    public boolean isDisabledForLevel(Level logLevel) {
        return !isEnabled(logLevel);
    }

}
