package com.wiredi.runtime.domain.errors.results.printer;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.lang.MapId;
import org.slf4j.event.Level;

import java.util.HashMap;
import java.util.Map;

public class LoggingErrorHandlingResultPrinter implements ErrorHandlingResultPrinter {

    private final Level level;
    private final Logging logger;

    private static final Map<MapId, LoggingErrorHandlingResultPrinter> printers = new HashMap<>();

    public static LoggingErrorHandlingResultPrinter get(Level level, Class<?> clazz) {
        return printers.computeIfAbsent(new MapId().add(clazz).add(level), k -> new LoggingErrorHandlingResultPrinter(level, Logging.getInstance(clazz)));
    }

    public LoggingErrorHandlingResultPrinter(Level level, Logging logger) {
        this.level = level;
        this.logger = logger;
    }

    @Override
    public void print(String message) {
        logger.log(level, message);
    }

    @Override
    public void print(String message, Throwable throwable) {
        logger.log(level, message, throwable);
    }

    @Override
    public void print(Throwable throwable) {
        logger.log(level, throwable.getMessage(), throwable);
    }
}
