package com.wiredi.compiler.tests.files.utils;

import com.wiredi.compiler.tests.files.InMemoryJavaFile;
import com.wiredi.compiler.tests.files.URIs;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static javax.tools.JavaFileObject.Kind.SOURCE;

public class JavaFileObjectFactory extends RootFolderAware {

    public JavaFileObjectFactory(String rootFolder) {
        super(rootFolder, '.');
    }

    public JavaFileObjectFactory() {
        this(null);
    }

    public JavaFileObject ofLines(String fullyQualifiedName, String... lines) {
        return new InMemoryJavaFile(resolveUri(fullyQualifiedName), SOURCE, String.join(System.lineSeparator(), lines), fullyQualifiedName);
    }

    public JavaFileObject ofLines(String fullyQualifiedName, Iterable<String> lines) {
        return new InMemoryJavaFile(resolveUri(fullyQualifiedName), SOURCE, String.join(System.lineSeparator(), lines), fullyQualifiedName);
    }

    public JavaFileObject ofLines(String fullyQualifiedName, String source) {
        return new InMemoryJavaFile(resolveUri(fullyQualifiedName), SOURCE, source, fullyQualifiedName);
    }

    public JavaFileObject load(String fullyQualifiedName) {
        return load(resolveUri(fullyQualifiedName), resolveConcreteName(fullyQualifiedName));
    }

    public List<JavaFileObject> loadFromFolder(String fullyQualifiedName) {
        return loadFromFolder(fullyQualifiedName, false);
    }

    public List<JavaFileObject> loadFromFolder(String fullyQualifiedName, boolean recursive) {
        Path rootFolder = Path.of(resolveUri(fullyQualifiedName));
        List<JavaFileObject> result = new ArrayList<>();
        if (!Files.isDirectory(rootFolder)) {
            throw new IllegalArgumentException("The provided path " + fullyQualifiedName + " is not a folder.");
        }

        try (Stream<Path> stream = Files.list(rootFolder)) {
            stream.forEach(sub -> {
                if (Files.isDirectory(sub) && recursive) {
                    result.addAll(loadFromFolder(fullyQualifiedName, true));
                } else if (Files.isRegularFile(sub) && sub.getFileName().toString().endsWith(SOURCE.extension)) {
                    result.add(load(sub.toUri(), fullyQualifiedName));
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return result;
    }

    public JavaFileObject load(URI resource, String name) {
        try {
            return load(resource.toURL(), name);
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    private JavaFileObject load(URL resource, String name) {
        try (var stream = resource.openStream()) {
            var uri = URIs.create(resource);
            return new InMemoryJavaFile(uri, URIs.deduceJavaFileKind(uri), stream.readAllBytes(), name);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
