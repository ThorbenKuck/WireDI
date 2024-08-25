package com.wiredi.compiler.tests.files.utils;

import com.wiredi.compiler.tests.files.URIs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URL;
import java.nio.file.Files;

import static javax.tools.JavaFileObject.Kind.SOURCE;

public class RootFolderAware {

    @Nullable
    private String rootFolder;
    private final char pathSeparator;

    public RootFolderAware(@Nullable String rootFolder, char pathSeparator) {
        this.rootFolder = rootFolder;
        this.pathSeparator = pathSeparator;
    }

    public void setRootFolder(String rootFolder) {
        this.rootFolder = rootFolder;
    }

    public String resolvePath(String path) {
        if (rootFolder == null || rootFolder.isBlank()) {
            return path;
        }

        if (path.charAt(path.length() - 1) == pathSeparator) {
            return rootFolder + path;
        }

        return rootFolder + pathSeparator + path;
    }

    protected URI resolveUri(String fullyQualifiedName) {
        return URIs.create(resolveUrl(fullyQualifiedName));
    }

    protected URL resolveUrl(String fullyQualifiedName) {
        String path = resolvePath(fullyQualifiedName);
        String name = path.replace('.', '/') + SOURCE.extension;

        var url = RootFolderAware.class.getClassLoader().getResource(name);
        if (url == null) {
            throw new IllegalArgumentException("\"" + path + "\" does not exist on the current classpath");
        }

        return url;
    }

    protected String resolveConcreteName(String fileName) {
        return resolvePath(fileName);
    }
}
