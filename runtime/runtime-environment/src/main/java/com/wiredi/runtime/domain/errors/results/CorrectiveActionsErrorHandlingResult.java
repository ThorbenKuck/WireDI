package com.wiredi.runtime.domain.errors.results;

import com.wiredi.runtime.domain.errors.results.printer.ErrorHandlingResultPrinter;
import com.wiredi.runtime.domain.errors.results.printer.PrintStreamErrorHandlingResultPrinter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.wiredi.runtime.lang.Preconditions.is;

public class CorrectiveActionsErrorHandlingResult<T extends Throwable> implements ErrorHandlingResult<T> {

    private final @NotNull T cause;
    private final @NotNull List<String> correctiveActions;
    private final @NotNull ErrorHandlingResultPrinter printer;

    public CorrectiveActionsErrorHandlingResult(
            @NotNull T cause,
            @NotNull List<@NotNull String> correctiveActions,
            @NotNull ErrorHandlingResultPrinter printer
    ) {
        is(!correctiveActions.isEmpty(), () -> "Cannot construct a CorrectiveActionsErrorHandlingResult without any corrective actions");
        this.cause = cause;
        this.correctiveActions = correctiveActions;
        this.printer = printer;
    }

    @Override
    public boolean apply() throws Throwable {
        printer.print("Encountered Exception " + cause.getClass().getSimpleName() + ": " + cause.getMessage());
        printer.print("Corrective Actions: ");
        correctiveActions.forEach(action -> printer.print(" - " + action));
        return true;
    }

    public static class Builder<T extends Throwable> {

        private final List<@NotNull String> correctiveActions = new ArrayList<>();
        private @NotNull ErrorHandlingResultPrinter printer = PrintStreamErrorHandlingResultPrinter.get(System.out);
        private final @NotNull T cause;

        public Builder(@NotNull T cause) {
            this.cause = cause;
        }

        public @NotNull Builder<T> addAction(@NotNull String action) {
            correctiveActions.add(action);
            return this;
        }

        public @NotNull Builder<T> printTo(@NotNull ErrorHandlingResultPrinter printer) {
            this.printer = printer;
            return this;
        }

        public @NotNull CorrectiveActionsErrorHandlingResult<T> build() {
            return new CorrectiveActionsErrorHandlingResult<T>(cause, correctiveActions, printer);
        }
    }
}
