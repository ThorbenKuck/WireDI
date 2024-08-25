package com.wiredi.compiler.tests.files;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FileManagerState {

    private final Map<URI, JavaFileObject> inputJavaFiles = new HashMap<>();
    private final Map<URI, FileObject> inputResources = new HashMap<>();
    private final Map<URI, JavaFileObject> outputJavaFiles = new HashMap<>();
    private final Map<URI, FileObject> outputResources = new HashMap<>();

    /**
     * Creates a URI using the given location, package and relative file name.
     *
     * @param location the location
     * @param pack     the package
     * @param relative the relative file name
     * @return a URI
     */
    public static URI uriOf(JavaFileManager.Location location, String pack, String relative) {
        return URI.create("mem:///" + URLEncoder.encode(location.getName(), StandardCharsets.UTF_8) + "/" + (pack.isEmpty() ? "" : (pack.replace('.', '/') + "/")) + relative);
    }

    /**
     * Creates a URI using the given location, class name and file kind.
     *
     * @param location the location
     * @param type     the class name
     * @param kind     the file kind
     * @return a URI
     */
    public static URI uriOf(JavaFileManager.Location location, String type, JavaFileObject.Kind kind) {
        return URI.create("mem:///" + URLEncoder.encode(location.getName(), StandardCharsets.UTF_8) + "/" + type.replace('.', '/') + kind.extension);
    }

    public void addInputResource(FileObject fileObject) {
        inputResources.put(fileObject.toUri(), fileObject);
    }

    public void addInputJavaFile(JavaFileObject javaFileObject) {
        inputJavaFiles.put(javaFileObject.toUri(), javaFileObject);
    }

    @NotNull
    public JavaFileObject getOrCreateInputJavaFile(JavaFileManager.Location location, String className, JavaFileObject.Kind kind) {
        return inputJavaFiles.computeIfAbsent(uriOf(location, className, kind), uri -> new InMemoryJavaFile(uri, className));
    }

    @NotNull
    public FileObject getOrCreateInputResource(JavaFileManager.Location location, String packageName, String relativeName) {
        return inputResources.computeIfAbsent(uriOf(location, packageName, relativeName), InMemoryFileObject::new);
    }

    @NotNull
    public JavaFileObject getOrCreateOutputJavaFile(JavaFileManager.Location location, String className, JavaFileObject.Kind kind) {
        return outputJavaFiles.computeIfAbsent(uriOf(location, className, kind), uri -> new InMemoryJavaFile(uri, className));
    }

    @NotNull
    public FileObject getOrCreateOutputResource(JavaFileManager.Location location, String packageName, String relativeName) {
        return outputResources.computeIfAbsent(uriOf(location, packageName, relativeName), InMemoryFileObject::new);
    }

    @Nullable
    public JavaFileObject getInputJavaFile(JavaFileManager.Location location, String className, JavaFileObject.Kind kind) {
        return inputJavaFiles.get(uriOf(location, className, kind));
    }

    @Nullable
    public FileObject getInputResource(JavaFileManager.Location location, String packageName, String relativeName) {
        return inputResources.get(uriOf(location, packageName, relativeName));
    }

    @Nullable
    public JavaFileObject getOutputJavaFile(JavaFileManager.Location location, String className, JavaFileObject.Kind kind) {
        return outputJavaFiles.get(uriOf(location, className, kind));
    }

    @Nullable
    public FileObject getOutputResource(JavaFileManager.Location location, String packageName, String relativeName) {
        return outputResources.get(uriOf(location, packageName, relativeName));
    }

    public Collection<JavaFileObject> getInputJavaFiles() {
        return this.inputJavaFiles.values();
    }

    public Collection<FileObject> getInputResourceFiles() {
        return this.inputResources.values();
    }

    public Collection<JavaFileObject> getOutputJavaFiles() {
        return this.outputJavaFiles.values();
    }

    public Collection<FileObject> getOutputResourceFiles() {
        return this.outputResources.values();
    }

    /**
     * Returns the generated Java source files.
     *
     * @return the generated Java source files
     */
    public List<JavaFileObject> generatedSources() {
        var result = new ArrayList<JavaFileObject>();
        var prefix = "/" + StandardLocation.SOURCE_OUTPUT.name();
        outputJavaFiles.forEach((uri, file) -> {
            if (uri.getPath().startsWith(prefix) && file.getKind() == JavaFileObject.Kind.SOURCE) {
                result.add(file);
            }
        });

        return result;
    }
}
