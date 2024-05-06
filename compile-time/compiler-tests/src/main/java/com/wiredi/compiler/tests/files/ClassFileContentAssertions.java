package com.wiredi.compiler.tests.files;

import org.opentest4j.AssertionFailedError;
import org.opentest4j.FileInfo;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.wiredi.compiler.tests.Assertions.tryRaise;

public class ClassFileContentAssertions {

    private final List<AssertionError> errors = new ArrayList<>();

    public static String removeLeadingWhiteSpaces(byte[] bytes) {
        return removeLeadingWhiteSpaces(new String(bytes));
    }

    public static String removeLeadingWhiteSpaces(String input) {
        return Arrays.stream(input.split("\n")).map(it -> {
            String line = it;
            while (line.startsWith(" ")) {
                line = line.replaceFirst(" ", "");
            }
            return line + '\n';
        }).collect(Collectors.joining());
    }

    public static boolean contentsMatch(JavaFileObject expected, JavaFileObject actual) {
        try {
            return match(expected.getCharContent(true), actual.getCharContent(true));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean match(CharSequence expected, CharSequence actual) {
        return removeLeadingWhiteSpaces(expected.toString()).equals(removeLeadingWhiteSpaces(actual.toString()));
    }

    public static boolean match(String expected, String actual) {
        return removeLeadingWhiteSpaces(expected).equals(removeLeadingWhiteSpaces(actual));
    }

    public static boolean match(byte[] expected, byte[] actual) {
        return removeLeadingWhiteSpaces(expected).equals(removeLeadingWhiteSpaces(actual));
    }

    public void assertContentsMatch(JavaFileObject expected, JavaFileObject actual) {
        if (!contentsMatch(expected, actual)) {
            addError(new AssertionFailedError("Contents of " + expected.getName() + " did not match " + actual.getName(), fileContentsOf(expected), fileContentsOf(actual)));
        }
    }

    public void addError(AssertionFailedError assertionFailedError) {
        errors.add(assertionFailedError);
    }

    public void assertNoErrors() {
        tryRaise("Multiple files did not match", errors);
    }

    private String fileContentsOf(JavaFileObject javaFileObject) {
        try {
            return javaFileObject.getCharContent(true).toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
