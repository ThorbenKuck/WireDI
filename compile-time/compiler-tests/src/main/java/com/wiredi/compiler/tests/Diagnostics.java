package com.wiredi.compiler.tests;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.List;

public final class Diagnostics implements DiagnosticListener<JavaFileObject> {
    
    /**
     * All diagnostic messages.
     */
    private final List<Diagnostic<? extends JavaFileObject>> all = new ArrayList<>();
    /**
     * All errors.
     */
    private final List<Diagnostic<? extends JavaFileObject>> errors = new ArrayList<>();
    /**
     * Mandatory and non-mandatory warnings.
     */
    private final List<Diagnostic<? extends JavaFileObject>> warnings = new ArrayList<>();
    /**
     * All notes.
     */
    private final List<Diagnostic<? extends JavaFileObject>> notes = new ArrayList<>();
    
    /**
     * Adds the given diagnostic to this {@code Diagnostics}.
     * 
     * @param diagnostic the diagnostic
     */
    @Override
    public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
        all.add(diagnostic);
        switch (diagnostic.getKind()) {
            case ERROR:
                errors.add(diagnostic);
                return;

            case MANDATORY_WARNING:
            case WARNING:
                warnings.add(diagnostic);
                return;

            case NOTE:
                notes.add(diagnostic);
        }
    }

    public List<Diagnostic<? extends JavaFileObject>> all() {
        return all;
    }

    public List<Diagnostic<? extends JavaFileObject>> errors() {
        return errors;
    }

    public List<Diagnostic<? extends JavaFileObject>> warnings() {
        return warnings;
    }

    public List<Diagnostic<? extends JavaFileObject>> notes() {
        return notes;
    }
}
