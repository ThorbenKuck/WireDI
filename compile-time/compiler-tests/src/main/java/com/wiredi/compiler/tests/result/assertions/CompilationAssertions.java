package com.wiredi.compiler.tests.result.assertions;

import com.wiredi.compiler.tests.result.Compilation;

import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class CompilationAssertions extends ErrorMessageAware<CompilationAssertions> {

    private final Compilation compilation;

    public CompilationAssertions(Compilation compilation) {
        this.compilation = compilation;
    }

    public CompilationAssertions wasSuccessful() {
        if (!compilation.success) {
            fail(() -> "The compilation should have been successful, but it failed." + System.lineSeparator()
                    + "Errors:" + System.lineSeparator()
                    + compilation.diagnostics().errors().stream().map(Object::toString).collect(Collectors.joining(System.lineSeparator(), " - ", "")));
        }

        return this;
    }

    public CompilationAssertions wasNotSuccessful() {
        assertFalse(compilation.success, getErrorMessage(() -> "The compilation should have failed, but it was successful."));
        return this;
    }

    public CompilationAssertions hasFailed() {
        return wasNotSuccessful();
    }

    public CompilationAssertions hasNoErrors() {
        hasDiagnosticsMatching(diagnostics -> diagnostics.errors(DiagnosticStageAssertions::areEmpty));
        return this;
    }

    public CompilationAssertions hasNoWarnings() {
        hasDiagnosticsMatching(diagnostics -> diagnostics.warnings(DiagnosticStageAssertions::areEmpty));
        return this;
    }

    public CompilationAssertions hasDiagnosticsMatching(Consumer<DiagnosticAssertions> consumer) {
        consumer.accept(new DiagnosticAssertions(compilation.diagnostics()));
        return this;
    }

    public CompilationAssertions hasFilesMatching(Consumer<FileManagerAssertions> consumer) {
        consumer.accept(new FileManagerAssertions(compilation.files()));
        return this;
    }
}
