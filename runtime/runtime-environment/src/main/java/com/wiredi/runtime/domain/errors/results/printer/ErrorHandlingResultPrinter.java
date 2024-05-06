package com.wiredi.runtime.domain.errors.results.printer;

import org.slf4j.event.Level;

import java.io.PrintStream;

public interface ErrorHandlingResultPrinter {

    static LoggingErrorHandlingResultPrinter get(Level level, Class<?> clazz) {
        return LoggingErrorHandlingResultPrinter.get(level, clazz);
    }

    static PrintStreamErrorHandlingResultPrinter get(PrintStream printStream) {
        return PrintStreamErrorHandlingResultPrinter.get(printStream);
    }

    void print(String message);

    void print(String message, Throwable throwable);

    void print(Throwable throwable);
}
