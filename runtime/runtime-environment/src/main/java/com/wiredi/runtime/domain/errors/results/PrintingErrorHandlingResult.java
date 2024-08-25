package com.wiredi.runtime.domain.errors.results;

import com.wiredi.runtime.domain.errors.results.printer.ErrorHandlingResultPrinter;

public class PrintingErrorHandlingResult<T extends Throwable> implements ExceptionHandlingResult<T> {

    private final T throwable;
    private final ErrorHandlingResultPrinter printer;

    public PrintingErrorHandlingResult(T throwable, ErrorHandlingResultPrinter printer) {
        this.throwable = throwable;
        this.printer = printer;
    }

    @Override
    public boolean apply() throws Throwable {
        printer.print(throwable);
        return true;
    }
}
