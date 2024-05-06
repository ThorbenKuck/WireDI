package com.wiredi.compiler.tests.result.assertions;

import com.wiredi.compiler.tests.Assertions;
import com.wiredi.compiler.tests.files.ClassFileContentAssertions;
import org.opentest4j.AssertionFailedError;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class JavaFileObjectAssertions extends ErrorMessageAware<JavaFileObjectAssertions> {

    private final FileManagerAssertions fileManagerAssertions;
    private final String name;
    private final List<JavaFileObject> content;

    public JavaFileObjectAssertions(FileManagerAssertions fileManagerAssertions, String name, List<JavaFileObject> content) {
        this.fileManagerAssertions = fileManagerAssertions;
        this.name = name;
        this.content = content;
    }

    public JavaFileObjectAssertions contain(JavaFileObject expected) {
        assertThat(content)
                .withFailMessage(getErrorMessage(() -> "The JavaFileObjects did not contain " + expected))
                .contains(expected);

        return this;
    }

    public JavaFileObjectAssertions containAll(Iterable<JavaFileObject> expected) {
        assertThat(content)
                .withFailMessage(getErrorMessage(() -> "The JavaFileObjects did not contain all the expected files"))
                .containsAll(expected);

        return this;
    }

    public JavaFileObjectAssertions containExactly(JavaFileObject... expected) {
        return containExactly(Arrays.asList(expected));
    }

    public JavaFileObjectAssertions containExactly(Iterable<JavaFileObject> expected) {
        int counter = 0;
        final Iterator<JavaFileObject> expectedIterator = expected.iterator();
        final Iterator<JavaFileObject> actualIterator = content.iterator();
        final ClassFileContentAssertions assertions = new ClassFileContentAssertions();
        while (expectedIterator.hasNext()) {
            JavaFileObject nextExpected = expectedIterator.next();
            if (actualIterator.hasNext()) {
                JavaFileObject nextActual = actualIterator.next();
                assertions.assertContentsMatch(nextExpected, nextActual);
            } else {
                assertions.addError(new AssertionFailedError("The actual element " + counter + " is missing!", nextExpected, null));
            }
            ++counter;
        }

        if (actualIterator.hasNext()) {
            assertions.addError(new AssertionFailedError("The expected element " + counter + " was missing!", null, actualIterator.next()));
        }

        assertions.assertNoErrors();
        return this;
    }

    public JavaFileObjectAssertions containExactlyInAnyOrder(Iterable<JavaFileObject> expected) {
        assertThat(content)
                .withFailMessage(getErrorMessage(() -> "The JavaFileObjects did not contain all the expected files"))
                .containsExactlyInAnyOrderElementsOf(expected);

        return this;
    }

    public JavaFileObjectAssertions containExactlyInAnyOrder(JavaFileObject... expected) {
        return containExactlyInAnyOrder(Arrays.asList(expected));
    }

    public JavaFileObjectAssertions areEmpty() {
        assertThat(content)
                .withFailMessage(getErrorMessage(() -> "Java files should have been empty, but where not"))
                .isEmpty();

        return this;
    }
}
