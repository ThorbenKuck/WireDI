package com.wiredi.compiler.tests.result.assertions;

import com.wiredi.compiler.tests.Diagnostics;

import java.util.function.Consumer;
import java.util.regex.Pattern;

public class DiagnosticAssertions extends ErrorMessageAware<DiagnosticAssertions> {

    private final Diagnostics diagnostics;

    public DiagnosticAssertions(Diagnostics diagnostics) {
        this.diagnostics = diagnostics;
    }

    public DiagnosticAssertions errors(Consumer<DiagnosticStageAssertions> consumer) {
        consumer.accept(errors());
        return this;
    }

    public DiagnosticStageAssertions errors() {
        return new DiagnosticStageAssertions(diagnostics.errors(), this, "error");
    }

    public DiagnosticAssertions containsError(String message) {
        return errors(it -> it.contain(message));
    }

    public DiagnosticAssertions containsError(Pattern pattern) {
        return errors(it -> it.contain(pattern));
    }

    public DiagnosticAssertions warnings(Consumer<DiagnosticStageAssertions> consumer) {
        consumer.accept(warnings());
        return this;
    }

    public DiagnosticStageAssertions warnings() {
        return new DiagnosticStageAssertions(diagnostics.warnings(), this, "warning");
    }

    public DiagnosticAssertions containsWarning(String message) {
        return warnings(it -> it.contain(message));
    }

    public DiagnosticAssertions containsWarning(Pattern pattern) {
        return warnings(it -> it.contain(pattern));
    }

    public DiagnosticAssertions notes(Consumer<DiagnosticStageAssertions> consumer) {
        consumer.accept(notes());
        return this;
    }

    public DiagnosticStageAssertions notes() {
        return new DiagnosticStageAssertions(diagnostics.notes(), this, "note");
    }

    public DiagnosticAssertions containsNote(String message) {
        return notes(it -> it.contain(message));
    }

    public DiagnosticAssertions containsNote(Pattern pattern) {
        return notes(it -> it.contain(pattern));
    }
}
