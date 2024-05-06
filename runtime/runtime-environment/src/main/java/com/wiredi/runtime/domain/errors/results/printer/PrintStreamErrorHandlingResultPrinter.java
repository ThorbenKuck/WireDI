package com.wiredi.runtime.domain.errors.results.printer;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class PrintStreamErrorHandlingResultPrinter implements ErrorHandlingResultPrinter {

    private final PrintStream printStream;

    private static final Map<PrintStream, PrintStreamErrorHandlingResultPrinter> printers = new HashMap<>();

    public static PrintStreamErrorHandlingResultPrinter get(PrintStream printStream) {
        return printers.computeIfAbsent(printStream, p -> new PrintStreamErrorHandlingResultPrinter(p));
    }

    public PrintStreamErrorHandlingResultPrinter(PrintStream printStream) {
        this.printStream = printStream;
    }

    @Override
    public void print(String message) {
        printStream.println(message);
    }

    @Override
    public void print(String message, Throwable throwable) {
        printStream.println(message);
        throwable.printStackTrace(printStream);
    }

    @Override
    public void print(Throwable throwable) {
        throwable.printStackTrace(printStream);
    }
}
