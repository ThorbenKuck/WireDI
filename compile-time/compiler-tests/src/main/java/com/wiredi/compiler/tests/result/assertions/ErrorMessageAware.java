package com.wiredi.compiler.tests.result.assertions;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class ErrorMessageAware<T extends ErrorMessageAware<T>> {

    private final ErrorMessage errorMessage = new ErrorMessage();

    public T withErrorMessage(Supplier<String> errorMessageSupplier, boolean preserve) {
        this.errorMessage.errorMessage = errorMessageSupplier;
        this.errorMessage.preserve = preserve;

        return (T) this;
    }

    public T withErrorMessage(String errorMessage, boolean preserve) {
        return withErrorMessage(() -> errorMessage, preserve);
    }

    public T withErrorMessage(Supplier<String> errorMessage) {
        return withErrorMessage(errorMessage, false);
    }

    public T withErrorMessage(String errorMessage) {
        return withErrorMessage(errorMessage, false);
    }

    protected Supplier<String> getErrorMessage(Supplier<String> defaultMessage) {
        return errorMessage.getErrorMessage(defaultMessage);
    }

    private static class ErrorMessage {
        @Nullable
        private Supplier<String> errorMessage;
        private boolean preserve;

        public Supplier<String> getErrorMessage(Supplier<String> defaultMessage) {
            if (errorMessage == null) {
                return defaultMessage;
            }

            Supplier<String> result = errorMessage;
            if (!preserve) {
                this.errorMessage = null;
            }

            return result;
        }
    }
}
