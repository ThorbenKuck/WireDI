package com.wiredi.compiler.tests.files.utils;

import com.wiredi.compiler.tests.files.URIs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

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
        String name = path.replace('.', '/');

        if (!name.endsWith(SOURCE.extension)) {
            name = name + SOURCE.extension;
        }

        // Zuerst versuchen, aus den Resources zu laden
        var url = getClass().getClassLoader().getResource(name);

        // Falls nicht gefunden, versuchen wir den Source-Pfad zu ermitteln
        if (url == null) {
            url = findSourceFile(fullyQualifiedName, name);
        }

        if (url == null) {
            throw new IllegalArgumentException("\"" + path + "\" does not exist on the current classpath");
        }

        return url;
    }

    private URL findSourceFile(String fullyQualifiedName, String resourcePath) {
        try {
            // Versuche die .class Datei zu finden, um den Basispfad zu ermitteln
            String classResourcePath = fullyQualifiedName.replace('.', '/') + ".class";
            URL classUrl = getClass().getClassLoader().getResource(classResourcePath);

            if (classUrl != null && "file".equals(classUrl.getProtocol())) {
                // Konvertiere z.B. /target/classes/com/example/Test.class
                // zu /src/main/java/com/example/Test.java
                String classPath = classUrl.getPath();

                // Versuche verschiedene typische Maven/Gradle Strukturen
                String[] possibleSourcePaths = {
                        classPath.replace("/target/classes/", "/src/main/java/").replace(".class", ".java"),
                        classPath.replace("/target/test-classes/", "/src/test/java/").replace(".class", ".java"),
                        classPath.replace("/build/classes/java/main/", "/src/main/java/").replace(".class", ".java"),
                        classPath.replace("/build/classes/java/test/", "/src/test/java/").replace(".class", ".java"),
                        classPath.replace("/out/production/", "/src/main/java/").replace(".class", ".java"),
                        classPath.replace("/out/test/", "/src/test/java/").replace(".class", ".java")
                };

                for (String sourcePath : possibleSourcePaths) {
                    Path path = Path.of(sourcePath);
                    if (Files.exists(path)) {
                        return path.toUri().toURL();
                    }
                }
            }
        } catch (Exception e) {
            // Ignorieren und null zurückgeben
        }

        return null;
    }

    protected String resolveConcreteName(String fileName) {
        return resolvePath(fileName);
    }
}
