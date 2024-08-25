package com.wiredi.compiler.tests.result.assertions;

import com.wiredi.compiler.tests.files.ClassFileContentAssertions;
import com.wiredi.compiler.tests.files.FileManagerState;
import com.wiredi.compiler.tests.files.utils.JavaFileObjectFactory;
import org.opentest4j.AssertionFailedError;

import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.wiredi.compiler.tests.Assertions.assertThat;
import static com.wiredi.compiler.tests.files.ClassFileContentAssertions.removeLeadingWhiteSpaces;
import static org.junit.jupiter.api.Assertions.fail;

public class FileManagerAssertions extends ErrorMessageAware<FileManagerAssertions> {

    private final FileManagerState fileManagerState;

    public FileManagerAssertions(FileManagerState fileManagerState) {
        this.fileManagerState = fileManagerState;
    }

    public JavaFileObjectAssertions generatedSources() {
        return new JavaFileObjectAssertions(this, "generated sources", fileManagerState.generatedSources());
    }

    public FileManagerAssertions containsGeneratedFile(String name) {
        return containsGeneratedFile(name, new JavaFileObjectFactory().load(name));
    }

    public FileManagerAssertions containsGeneratedFile(String className, JavaFileObject expectedSource) {
        String name = "/SOURCE_OUTPUT/" + className.replaceAll("\\.", "/") + ".java";

        List<JavaFileObject> sources = fileManagerState.generatedSources()
                .stream()
                .filter(it -> it.getName().equals(expectedSource.getName()))
                .toList();

        if (sources.size() != 1) {
            throw new AssertionFailedError("Expected to find exactly one class with the name " + className + " but found " + sources.size() + " in " + fileManagerState.generatedSources().stream().map(FileObject::getName).toList(), name, fileManagerState.generatedSources().stream().map(FileObject::getName).toList());
        }

        JavaFileObject actualSource = sources.getFirst();
        try(InputStream actualInputStream = actualSource.openInputStream();
            InputStream expectedInputStream = expectedSource.openInputStream()) {
            String actualContent = removeLeadingWhiteSpaces(actualInputStream.readAllBytes());
            String expectedContent = removeLeadingWhiteSpaces(expectedInputStream.readAllBytes());

            if (!ClassFileContentAssertions.match(actualInputStream.readAllBytes(), expectedInputStream.readAllBytes())) {
                throw new AssertionFailedError("The class " + className + " was found, but did not match the expected one", expectedContent, actualContent);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public FileManagerAssertions containsGeneratedFile(JavaFileObject expectedSource) {
        String name = "/SOURCE_OUTPUT/" + expectedSource.getName();

        List<JavaFileObject> sources = fileManagerState.generatedSources()
                .stream()
                .filter(it -> it.getName().equals(expectedSource.getName()))
                .toList();

        if (sources.size() != 1) {
            throw new AssertionFailedError("Expected to find exactly one class with the name " + expectedSource.getName() + " but found " + sources.size(), name, fileManagerState.generatedSources().stream().map(FileObject::getName).toList());
        }

        JavaFileObject actualSource = sources.getFirst();
        try(InputStream actualInputStream = actualSource.openInputStream();
            InputStream expectedInputStream = expectedSource.openInputStream()) {
            String actualContent = removeLeadingWhiteSpaces(actualInputStream.readAllBytes());
            String expectedContent = removeLeadingWhiteSpaces(expectedInputStream.readAllBytes());

            if (!ClassFileContentAssertions.match(actualInputStream.readAllBytes(), expectedInputStream.readAllBytes())) {
                throw new AssertionFailedError("The class " + expectedSource.getName() + " was found, but did not match the expected one", expectedContent, actualContent);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public FileManagerAssertions containsAllGeneratedFiles(JavaFileObject... expected) {
        return containsAllGeneratedFiles(Arrays.asList(expected));
    }

    public FileManagerAssertions containsAllGeneratedFiles(Collection<JavaFileObject> expected) {
        assertThat(fileManagerState.generatedSources())
                .containsAll(expected);

        return this;
    }

    public FileManagerAssertions containsExactlyAllGeneratedFiles(Collection<JavaFileObject> expected) {
        assertThat(fileManagerState.generatedSources())
                .containsExactly(expected);

        return this;
    }

    public FileManagerAssertions containsExactlyAllGeneratedFiles(JavaFileObject... expected) {
        return containsExactlyAllGeneratedFiles(Arrays.asList(expected));
    }
}
