package com.wiredi.compiler.tests.files.utils;

import com.wiredi.compiler.tests.files.InMemoryFileObject;
import com.wiredi.compiler.tests.files.URIs;
import org.jetbrains.annotations.Nullable;

import javax.tools.FileObject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class ResourceFileObjectFactory extends RootFolderAware {

    public ResourceFileObjectFactory(@Nullable String rootFolder) {
        super(rootFolder, '/');
    }

    public ResourceFileObjectFactory() {
        this(null);
    }

    public InMemoryFileObject ofLines(String fullyQualifiedName, String... lines) {
        return new InMemoryFileObject(resolveUri(fullyQualifiedName), String.join(System.lineSeparator(), lines));
    }

    public InMemoryFileObject ofLines(String fullyQualifiedName, Iterable<String> lines) {
        return new InMemoryFileObject(resolveUri(fullyQualifiedName), String.join(System.lineSeparator(), lines));
    }

    public InMemoryFileObject ofLines(String fullyQualifiedName, String source) {
        return new InMemoryFileObject(resolveUri(fullyQualifiedName), source);
    }


    public FileObject load(String resource) {
        var url = JavaFileObjectFactory.class.getClassLoader().getResource(resource);
        if (url == null) {
            throw new IllegalArgumentException("\"" + resource + "\" does not exist on the current classpath");
        }

        return load(url);
    }

    public FileObject load(URI resource) {
        try (var stream = resource.toURL().openStream()) {
            return new InMemoryFileObject(resource, stream.readAllBytes());
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public FileObject load(URL resource) {
        try (var stream = resource.openStream()) {
            var uri = URIs.create(resource);
            return new InMemoryFileObject(uri, stream.readAllBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
