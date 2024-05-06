package com.wiredi.runtime.domain.errors.results;

import com.wiredi.runtime.domain.errors.ErrorHandler;
import com.wiredi.runtime.domain.errors.results.printer.ErrorHandlingResultPrinter;
import com.wiredi.runtime.domain.errors.results.printer.LoggingErrorHandlingResultPrinter;
import com.wiredi.runtime.domain.errors.results.printer.PrintStreamErrorHandlingResultPrinter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.event.Level;

import java.io.PrintStream;

public interface ErrorHandlingResult<T extends Throwable> {

    static <T extends Throwable> ErrorHandlingResult<T> doNothing() {
        return (ErrorHandlingResult<T>) NoOpErrorHandlingResult.INSTANCE;
    }

    static <T extends Throwable> ErrorHandlingResult<T> notProcessed() {
        return (ErrorHandlingResult<T>) NotProcessedErrorHandlingResult.INSTANCE;
    }

    static <T extends Throwable> ErrorHandlingResult<T> printException(T throwable) {
        return printException(throwable, System.out);
    }

    static <T extends Throwable> ErrorHandlingResult<T> printException(T throwable, PrintStream printStream) {
        return new PrintingErrorHandlingResult<>(throwable, PrintStreamErrorHandlingResultPrinter.get(printStream));
    }

    static <T extends Throwable> ErrorHandlingResult<T> logException(T throwable) {
        return logException(throwable, Level.ERROR);
    }

    static <T extends Throwable> ErrorHandlingResult<T> logException(T throwable, Level level) {
        return logException(throwable, level, ErrorHandler.class);
    }

    static <T extends Throwable> ErrorHandlingResult<T> logException(T throwable, Level level, Class<?> loggerClass) {
        return new PrintingErrorHandlingResult<>(throwable, LoggingErrorHandlingResultPrinter.get(level, loggerClass));
    }

    static <T extends Throwable> ErrorHandlingResult<T> rethrow(T throwable) {
        return new RethrowingErrorHandlingResult<>(throwable);
    }

    static <T extends Throwable> CorrectiveActionsErrorHandlingResult.Builder<T> correctable(T throwable) {
        return new CorrectiveActionsErrorHandlingResult.Builder<>(throwable);
    }

    @NotNull
    default ErrorHandlingResult<T> then(@NotNull ErrorHandlingResult<T> error) {
        return new SequenceErrorHandlingResult<T>().append(this).append(error);
    }

    /**
     * Executes the Processing result and returns whether it is successful or not.
     * <p>
     * If the method returns true, the connected {@link ErrorHandler} successfully processed the exception.
     * <p>
     * If the method returns false, the next {@link ErrorHandler} will be asked to process the exception.
     *
     * @return whether the {@link ErrorHandler} processed the throwable
     */
    boolean apply() throws Throwable;

}
