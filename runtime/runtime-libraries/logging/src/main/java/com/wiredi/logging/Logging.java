package com.wiredi.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A functional wrapper around the slf4j Logger.
 * <p>
 * It's main task is to provide functions that take lambdas when logging, to make logging more convenient.
 */
public class Logging {

    private final Logger logger;
    private boolean enabled = true;

    private Logging(Logger logger) {
        this.logger = logger;
    }

    public static Logging getInstance(Class<?> target) {
        return getInstance(target.getName());
    }

    public static Logging getInstance(String name) {
        return new Logging(LoggerFactory.getLogger(name));
    }

    public Logging setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    private boolean isEnabled(Level level) {
        return enabled && logger.isEnabledForLevel(level);
    }

    public void trace(Supplier<String> message) {
        if (isEnabled(Level.TRACE)) {
            logger.trace(message.get());
        }
    }

    public void trace(String message) {
        if (isEnabled(Level.TRACE)) {
            logger.trace(message);
        }
    }

    public void trace(Supplier<String> message, Throwable throwable) {
        if (isEnabled(Level.TRACE)) {
            logger.trace(message.get(), throwable);
        }
    }

    public void trace(String message, Throwable throwable) {
        if (isEnabled(Level.TRACE)) {
            logger.trace(message, throwable);
        }
    }

    public void debug(Supplier<String> message) {
        if (isEnabled(Level.DEBUG)) {
            logger.debug(message.get());
        }
    }

    public void debug(String message) {
        if (isEnabled(Level.DEBUG)) {
            logger.debug(message);
        }
    }

    public void debug(Supplier<String> message, Throwable throwable) {
        if (isEnabled(Level.DEBUG)) {
            logger.debug(message.get(), throwable);
        }
    }

    public void debug(String message, Throwable throwable) {
        if (isEnabled(Level.DEBUG)) {
            logger.debug(message, throwable);
        }
    }

    public void info(Supplier<String> message) {
        if (isEnabled(Level.INFO)) {
            logger.info(message.get());
        }
    }

    public void info(String message) {
        if (isEnabled(Level.INFO)) {
            logger.info(message);
        }
    }

    public void info(Supplier<String> message, Throwable throwable) {
        if (isEnabled(Level.INFO)) {
            logger.info(message.get(), throwable);
        }
    }

    public void info(String message, Throwable throwable) {
        if (isEnabled(Level.INFO)) {
            logger.info(message, throwable);
        }
    }

    public void warn(Supplier<String> message) {
        if (isEnabled(Level.WARN)) {
            logger.warn(message.get());
        }
    }

    public void warn(String message) {
        if (isEnabled(Level.WARN)) {
            logger.warn(message);
        }
    }

    public void warn(Supplier<String> message, Throwable throwable) {
        if (isEnabled(Level.WARN)) {
            logger.warn(message.get(), throwable);
        }
    }

    public void warn(String message, Throwable throwable) {
        if (isEnabled(Level.WARN)) {
            logger.warn(message, throwable);
        }
    }

    public void error(Supplier<String> message) {
        if (isEnabled(Level.ERROR)) {
            logger.error(message.get());
        }
    }

    public void error(String message) {
        if (isEnabled(Level.ERROR)) {
            logger.error(message);
        }
    }

    public void error(Supplier<String> message, Throwable throwable) {
        if (isEnabled(Level.ERROR)) {
            logger.error(message.get(), throwable);
        }
    }

    public void error(String message, Throwable throwable) {
        if (isEnabled(Level.ERROR)) {
            logger.error(message, throwable);
        }
    }

    public void log(Level level, Supplier<String> message) {
        if (isEnabled(level)) {
            logger.atLevel(level).log(message.get());
        }
    }

    public void log(Level level, String message) {
        if (isEnabled(level)) {
            logger.atLevel(level).log(message);
        }
    }

    public void log(Level level, Supplier<String> message, Throwable throwable) {
        if (isEnabled(level)) {
            logger.atLevel(level).log(message.get(), throwable);
        }
    }

    public void log(Level level, String message, Throwable throwable) {
        if (isEnabled(level)) {
            logger.atLevel(level).log(message, throwable);
        }
    }
}
