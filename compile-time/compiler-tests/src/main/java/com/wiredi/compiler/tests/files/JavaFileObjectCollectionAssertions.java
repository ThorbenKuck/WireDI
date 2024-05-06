package com.wiredi.compiler.tests.files;

import com.wiredi.compiler.tests.Assertions;
import com.wiredi.compiler.tests.result.assertions.ErrorMessageAware;
import org.opentest4j.AssertionFailedError;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaFileObjectCollectionAssertions extends ErrorMessageAware<JavaFileObjectCollectionAssertions> {

    private final List<JavaFileObject> actual;

    public JavaFileObjectCollectionAssertions(List<JavaFileObject> actual) {
        this.actual = actual;
    }

    private CharSequence getContent(JavaFileObject javaFileObject) {
        try {
            return javaFileObject.getCharContent(true);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public JavaFileObjectCollectionAssertions contains(JavaFileObject expected) {
        CharSequence expectedContent = getContent(expected);

        if (actual.size() == 1) {
            assertEquals(expectedContent, getContent(actual.getFirst()), getErrorMessage(() -> "Expected the generated sources to contain " + expected + " but didn't"));
        } else {
            assertThat(actual.stream().map(it -> {
                try {
                    return it.getCharContent(false);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            })).withFailMessage(getErrorMessage(() -> "Expected the generated sources to contain " + expected + " but didn't"))
                    .contains(expectedContent);
        }

        return this;
    }

    public JavaFileObjectCollectionAssertions containsAll(Iterable<JavaFileObject> expected) {
        ComparisonsState comparisonsState = new ComparisonsState(expected);
        while (comparisonsState.hasNextExpected()) {
            comparisonsState.assertNextEqual();
        }

        comparisonsState.assertNoErrors();
        return this;
    }

    public JavaFileObjectCollectionAssertions containsExactly(Iterable<JavaFileObject> expected) {
        ComparisonsState comparisonsState = new ComparisonsState(expected);
        while (comparisonsState.hasNext()) {
            comparisonsState.assertNextEqual();
        }

        comparisonsState.assertNoErrors();
        return this;
    }

    class ComparisonsState {
        private final Iterator<JavaFileObject> expectedIterator;
        private final Iterator<JavaFileObject> actualIterator;
        private int index = 0;
        private final List<AssertionFailedError> errors = new ArrayList<>();

        ComparisonsState(Iterable<JavaFileObject> expected) {
            this.expectedIterator = expected.iterator();
            this.actualIterator = actual.iterator();
        }

        public boolean hasNextExpected() {
            return expectedIterator.hasNext();
        }

        public boolean hasNextActual() {
            return actualIterator.hasNext();
        }

        public boolean hasNext() {
            return hasNextActual() || hasNextExpected();
        }

        public boolean assertNextEqual() {
            if (expectedIterator.hasNext() && actualIterator.hasNext()) {
                JavaFileObject expected = expectedIterator.next();
                JavaFileObject actual = actualIterator.next();
                if (!ClassFileContentAssertions.match(getContent(expected), getContent(actual))) {
                    try {
                        errors.add(new AssertionFailedError(expected.getName() + " did not match " + actual.getName(), expected.getCharContent(true), actual.getCharContent(true)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else if (expectedIterator.hasNext()) {
                throw new AssertionFailedError("Expected to find a next actual value at index " + index + ", but did not find one", getContent(expectedIterator.next()), null);
            } else if (actualIterator.hasNext()) {
                throw new AssertionFailedError("Expected to find a next expected value at index " + index + ", but did not find one", null, getContent(actualIterator.next()));
            } else {
                return false;
            }

            ++index;
            return true;
        }

        public void assertNoErrors() {
            Assertions.tryRaise("Multiple java files did not match", errors);
        }
    }
}
