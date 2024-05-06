package com.wiredi.compiler.tests;

import com.wiredi.compiler.tests.files.FileManagerState;
import com.wiredi.compiler.tests.files.JavaFileObjectCollectionAssertions;
import com.wiredi.compiler.tests.result.Compilation;
import com.wiredi.compiler.tests.result.assertions.CompilationAssertions;
import com.wiredi.compiler.tests.result.assertions.DiagnosticAssertions;
import com.wiredi.compiler.tests.result.assertions.FileManagerAssertions;
import org.opentest4j.MultipleFailuresError;

import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.List;

public class Assertions {

    public static CompilationAssertions assertThat(Compilation compilation) {
        return new CompilationAssertions(compilation);
    }

    public static DiagnosticAssertions assertThat(Diagnostics diagnostics) {
        return new DiagnosticAssertions(diagnostics);
    }

    public static FileManagerAssertions assertThat(FileManagerState state) {
        return new FileManagerAssertions(state);
    }

    public static JavaFileObjectCollectionAssertions assertThat(List<JavaFileObject> fileObjectList) {
        return new JavaFileObjectCollectionAssertions(fileObjectList);
    }

    public static void tryRaise(String message, Iterable<? extends AssertionError> iterable) {
        List<AssertionError> errors = new ArrayList<>();
        iterable.iterator().forEachRemaining(errors::add);

        if (errors.size() == 1) {
            throw errors.getFirst();
        }
        if (errors.size() > 1) {
            throw new MultipleFailuresError(message, errors);
        }
    }
}
