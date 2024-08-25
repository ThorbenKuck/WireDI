package com.wiredi.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.function.Supplier;

/**
 * A functional wrapper around the slf4j Logger.
 * <p>
 * It's main task is to provide functions that take lambdas when logging, to make logging more convenient.
 */
public class Logging {

    private final Logger logger;

    public Logging(Logger logger) {
        this.logger = logger;
    }

    public static Logging getInstance(Class<?> target) {
        return getInstance(target.getName());
    }

    public static Logging getInstance(String name) {
        return new Logging(LoggerFactory.getLogger(name));
    }

    public void trace(Supplier<String> message) {
        if (logger.isTraceEnabled()) {
            logger.trace(message.get());
        }
    }

    public void trace(String message) {
        if (logger.isTraceEnabled()) {
            logger.trace(message);
        }
    }

    public void trace(Supplier<String> message, Throwable throwable) {
        if (logger.isTraceEnabled()) {
            logger.trace(message.get(), throwable);
        }
    }

    public void trace(String message, Throwable throwable) {
        if (logger.isTraceEnabled()) {
            logger.trace(message, throwable);
        }
    }

    public void debug(Supplier<String> message) {
        if (logger.isDebugEnabled()) {
            logger.debug(message.get());
        }
    }

    public void debug(String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(message);
        }
    }

    public void debug(Supplier<String> message, Throwable throwable) {
        if (logger.isDebugEnabled()) {
            logger.debug(message.get(), throwable);
        }
    }

    public void debug(String message, Throwable throwable) {
        if (logger.isDebugEnabled()) {
            logger.debug(message, throwable);
        }
    }

    public void info(Supplier<String> message) {
        if (logger.isInfoEnabled()) {
            logger.info(message.get());
        }
    }

    public void info(String message) {
        if (logger.isInfoEnabled()) {
            logger.info(message);
        }
    }

    public void info(Supplier<String> message, Throwable throwable) {
        if (logger.isInfoEnabled()) {
            logger.info(message.get(), throwable);
        }
    }

    public void info(String message, Throwable throwable) {
        if (logger.isInfoEnabled()) {
            logger.info(message, throwable);
        }
    }

    public void warn(Supplier<String> message) {
        if (logger.isWarnEnabled()) {
            logger.warn(message.get());
        }
    }

    public void warn(String message) {
        if (logger.isWarnEnabled()) {
            logger.warn(message);
        }
    }

    public void warn(Supplier<String> message, Throwable throwable) {
        if (logger.isWarnEnabled()) {
            logger.warn(message.get(), throwable);
        }
    }

    public void warn(String message, Throwable throwable) {
        if (logger.isWarnEnabled()) {
            logger.warn(message, throwable);
        }
    }

    public void error(Supplier<String> message) {
        if (logger.isErrorEnabled()) {
            logger.error(message.get());
        }
    }

    public void error(String message) {
        if (logger.isErrorEnabled()) {
            logger.error(message);
        }
    }

    public void error(Supplier<String> message, Throwable throwable) {
        if (logger.isErrorEnabled()) {
            logger.error(message.get(), throwable);
        }
    }

    public void error(String message, Throwable throwable) {
        if (logger.isErrorEnabled()) {
            logger.error(message, throwable);
        }
    }

    public void log(Level level, Supplier<String> message) {
        if (logger.isEnabledForLevel(level)) {
            logger.atLevel(level).log(message.get());
        }
    }

    public void log(Level level, String message) {
        if (logger.isEnabledForLevel(level)) {
            logger.atLevel(level).log(message);
        }
    }

    public void log(Level level, Supplier<String> message, Throwable throwable) {
        if (logger.isEnabledForLevel(level)) {
            logger.atLevel(level).log(message.get(), throwable);
        }
    }

    public void log(Level level, String message, Throwable throwable) {
        if (logger.isEnabledForLevel(level)) {
            logger.atLevel(level).log(message, throwable);
        }
    }
}
