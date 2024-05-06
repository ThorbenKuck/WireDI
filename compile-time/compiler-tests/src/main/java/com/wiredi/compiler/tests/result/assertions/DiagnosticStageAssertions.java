package com.wiredi.compiler.tests.result.assertions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.assertj.core.api.Assertions.*;

public class DiagnosticStageAssertions extends ErrorMessageAware<DiagnosticStageAssertions> {

    private final List<Diagnostic<? extends JavaFileObject>> diagnostics;
    private final DiagnosticAssertions parent;
    private final String singular;

    public DiagnosticStageAssertions(
            List<Diagnostic<? extends JavaFileObject>> diagnostics,
            DiagnosticAssertions parent,
            String singular
    ) {
        this.diagnostics = diagnostics;
        this.parent = parent;
        this.singular = singular;
    }

    private String singularName() {
        return singular;
    }

    private String pluralName() {
        return singular + "s";
    }

    private String contextualPlurality(int count) {
        if (count == 1) {
            return singularName();
        } else {
            return pluralName();
        }
    }

    public DiagnosticStageAssertions areEmpty() {
        return haveSize(0);
    }

    public DiagnosticStageAssertions haveSize(int expected) {
        assertEquals(expected, diagnostics.size(), getErrorMessage(() -> {
            String name = contextualPlurality(expected);
            StringBuilder result = new StringBuilder().append("Expected ")
                    .append(expected)
                    .append(" ")
                    .append(name)
                    .append(", but got ")
                    .append(diagnostics.size())
                    .append(" ")
                    .append(name)
                    .append(". All Messages:")
                    .append(System.lineSeparator());
            diagnostics.forEach(error -> result.append(" - ").append("[").append(error.getKind()).append("]: ").append(error.getMessage(null)).append(System.lineSeparator()));
            return result.toString();
        }));

        return this;
    }

    public DiagnosticStageAssertions contain(String expected) {
        return contain(expected, null);
    }

    public DiagnosticStageAssertions contain(@NotNull String expected, @Nullable Locale locale) {
        if (diagnostics.isEmpty()) {
            fail(getErrorMessage(() -> "Cannot find the " + singularName() + "-message " + System.lineSeparator() + "\"" + expected + "\"" + System.lineSeparator() + "No " + pluralName() + " have been reported"));
        }

        for (Diagnostic<? extends JavaFileObject> error : diagnostics) {
            String errorMessage = error.getMessage(locale);
            if (errorMessage.equals(expected)) {
                return this;
            }
        }

        return fail(errorMessageMissingMessage(expected, locale));
    }

    public DiagnosticStageAssertions contain(Pattern pattern) {
        return contain(pattern, null);
    }

    public DiagnosticStageAssertions contain(Pattern pattern, Locale locale) {
        if (diagnostics.isEmpty()) {
            fail(getErrorMessage(() -> "Cannot find the " + singularName() + "-message " + System.lineSeparator() + "\"" + pattern + "\"" + System.lineSeparator() + "No " + pluralName() + "have been reported"));
        }
        Predicate<String> matchPredicate = pattern.asMatchPredicate();

        for (Diagnostic<? extends JavaFileObject> error : diagnostics) {
            String errorMessage = error.getMessage(locale);
            if (matchPredicate.test(errorMessage)) {
                return this;
            }
        }

        return fail(errorMessageMissingMessage(pattern.pattern(), locale));
    }

    public DiagnosticAssertions and() {
        return parent;
    }

    @NotNull
    private Supplier<String> errorMessageMissingMessage(@NotNull String message, @Nullable Locale locale) {
        return getErrorMessage(() -> {
            StringBuilder result = new StringBuilder("Could not find the expected ")
                    .append(singularName())
                    .append("-message ")
                    .append(System.lineSeparator())
                    .append('"')
                    .append(message)
                    .append('"')
                    .append(System.lineSeparator())
                    .append("in:")
                    .append(System.lineSeparator());
            diagnostics.forEach(error -> result.append(" - ").append(error.getMessage(locale)).append(System.lineSeparator()));

            return result.toString();
        });
    }
}
