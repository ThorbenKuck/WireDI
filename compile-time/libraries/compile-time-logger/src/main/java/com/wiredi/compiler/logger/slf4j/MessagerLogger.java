package com.wiredi.compiler.logger.slf4j;

import com.wiredi.compiler.logger.pattern.CompiledLogPattern;
import com.wiredi.compiler.logger.LogPattern;
import com.wiredi.runtime.async.DataAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.AbstractLogger;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MessagerLogger extends AbstractLogger {

    private static final DataAccess messagerLock = new DataAccess();
    private final List<BufferedLog> buffer = new ArrayList<>();
    private final String name;
    private final Level level;
    private final LogPattern logPattern;
    private final boolean logToConsole;
    private volatile Messager messager;

    public MessagerLogger(
            @NotNull String name,
            @NotNull Level level,
            @NotNull LogPattern logPattern,
            boolean logToConsole,
            @Nullable Messager messager
    ) {
        this.name = name;
        this.level = level;
        this.logPattern = logPattern;
        this.logToConsole = logToConsole;
        this.messager = messager;
    }

    @Override
    public boolean isEnabledForLevel(Level level) {
        return this.level.toInt() <= level.toInt();
    }

    public void initialize(Messager m) {
//        if (messager != null) {
//            throw new IllegalStateException("The logger is already initialized");
//        }
        messagerLock.write(() -> {
            messager = m;
            for (BufferedLog entry : buffer) {
                entry.context.run(() -> {
                    if (entry.throwable != null) {
                        doLog(entry.level, entry.message, entry.throwable);
                    } else {
                        doLog(entry.level, entry.message);
                    }
                });
            }
            buffer.clear();
        });
    }

    public void doLog(Level level, String message) {
        Diagnostic.Kind kind = mapLevelToKind(level);
        LogPattern logMessage = logPattern.newInstance()
                .context("level", level.name())
                .context("thread", Thread.currentThread().getName())
                .context("message", message)
                .context("type", name)
                .context("kind", kind.name())
                .context("annotation", Optional.ofNullable(MessagerContext.get().getAnnotationType()).map(Class::getSimpleName).orElse(""))
                .context("annotationValue", Optional.ofNullable(MessagerContext.get().getAnnotationValue()).map(it -> it.getValue().toString()).orElse(""))
                .context("annotationMirror", Optional.ofNullable(MessagerContext.get().getAnnotationMirror()).map(it -> it.getAnnotationType().asElement().getSimpleName().toString()).orElse(""))
                .context("origin", Optional.ofNullable(MessagerContext.get().getElement()).map(it -> it.getKind().name() + " " + it.getSimpleName()).orElse(""));

        messagerLock.read(() -> {
            MessagerContext.Instance context = MessagerContext.get();
            if (messager != null) {
                String formated = logMessage.compile().format();
                messager.printMessage(kind, formated, context.getElement(), context.getAnnotationMirror(), context.getAnnotationValue());
                if (logToConsole) {
                    System.out.println(formated);
                }
            } else {
                buffer.add(new BufferedLog(level, message, context.copy(), null));
            }
        });
    }

    public void doLog(Level level, String message, Throwable throwable) {
        Diagnostic.Kind kind = mapLevelToKind(level);
        LogPattern logMessage = logPattern.newInstance()
                .context("level", level.name())
                .context("thread", Thread.currentThread().getName())
                .context("message", message)
                .context("type", name)
                .context("error", throwable.getLocalizedMessage())
                .context("errorType", throwable.getClass().getSimpleName())
                .context("kind", kind.name())
                .context("annotation", Optional.ofNullable(MessagerContext.get().getAnnotationType()).map(Class::getSimpleName).orElse(""))
                .context("annotationValue", Optional.ofNullable(MessagerContext.get().getAnnotationValue()).map(it -> it.getValue().toString()).orElse(""))
                .context("annotationMirror", Optional.ofNullable(MessagerContext.get().getAnnotationMirror()).map(it -> it.getAnnotationType().asElement().getSimpleName().toString()).orElse(""))
                .context("origin", Optional.ofNullable(MessagerContext.get().getElement()).map(it -> it.getKind().name() + " " + it.getSimpleName()).orElse(""));

        messagerLock.read(() -> {
            MessagerContext.Instance context = MessagerContext.get();
            if (messager != null) {
                String formated = logMessage.compile().format();
                messager.printMessage(kind, formated, context.getElement(), context.getAnnotationMirror(), context.getAnnotationValue());
                if (logToConsole) {
                    System.out.println(formated);
                }
                throwable.printStackTrace(System.out);
            } else {
                buffer.add(new BufferedLog(level, message, context.copy(), null));
            }
        });
    }

    public boolean isEnabled(Level level) {
        return this.level.toInt() <= level.toInt();
    }

    @Override
    protected String getFullyQualifiedCallerName() {
        return null;
    }

    @Override
    protected void handleNormalizedLoggingCall(
            Level level,
            Marker marker,
            String messagePattern,
            Object[] arguments,
            Throwable throwable
    ) {
        FormattingTuple ft = MessageFormatter.arrayFormat(messagePattern, arguments);
        String result = ft.getMessage();

        if (marker != null) {
            result = "[" + marker.getName() + "] " + result;
        }

        if (throwable != null) {
            result += " (" + throwable + ")";
        }

        doLog(level, result);
    }

    private Diagnostic.Kind mapLevelToKind(Level level) {
        return switch (level) {
            case ERROR -> Diagnostic.Kind.ERROR;
            case WARN -> Diagnostic.Kind.WARNING;
            default -> Diagnostic.Kind.NOTE;
        };
    }

    // Always log everything — refine later if needed
    @Override
    public boolean isTraceEnabled() {
        return isEnabled(Level.TRACE);
    }

    @Override
    public boolean isDebugEnabled() {
        return isEnabled(Level.DEBUG);
    }

    @Override
    public boolean isInfoEnabled() {
        return isEnabled(Level.INFO);
    }

    @Override
    public boolean isWarnEnabled() {
        return isEnabled(Level.WARN);
    }

    @Override
    public boolean isErrorEnabled() {
        return isEnabled(Level.ERROR);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return isEnabled(Level.TRACE);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return isEnabled(Level.DEBUG);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return isEnabled(Level.INFO);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return isEnabled(Level.WARN);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return isEnabled(Level.ERROR);
    }

    private record BufferedLog(
            Level level,
            String message,
            MessagerContext.Instance context,
            @Nullable Throwable throwable
    ) {
    }
}
