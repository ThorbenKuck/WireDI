package com.wiredi.compiler.tests.result;

import com.wiredi.compiler.tests.Diagnostics;
import com.wiredi.compiler.tests.files.FileManagerState;
import com.wiredi.compiler.tests.result.assertions.CompilationAssertions;

public class Compilation {

    private final FileManagerState fileManagerState;

    private final Diagnostics diagnostics;

    /**
     * Whether compilation was successful.
     */
    public final boolean success;

    public Compilation(FileManagerState fileManagerState, Diagnostics diagnostics, boolean success) {
        this.fileManagerState = fileManagerState;
        this.diagnostics = diagnostics;
        this.success = success;
    }

    public CompilationAssertions assertThat() {
        return new CompilationAssertions(this);
    }

    public CompilationAssertions andAssertThat() {
        return assertThat();
    }

    public Diagnostics diagnostics() {
        return diagnostics;
    }

    public FileManagerState files() {
        return fileManagerState;
    }
}
