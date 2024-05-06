package com.wiredi.compiler.tests.files;

import org.jetbrains.annotations.Nullable;

import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * A file manager that stores all output in memory.
 */
public class InMemoryJavaFileManager implements StandardJavaFileManager {

    private final FileManagerState state;
    private final StandardJavaFileManager delegate;

    /**
     * Creates a {@code FowardingFileManager} with the given underlying manager.
     *
     * @param manager the underlying manager to which all calls are forwarded
     */
    public InMemoryJavaFileManager(FileManagerState state, StandardJavaFileManager manager) {
        this.state = state;
        this.delegate = manager;
    }

    /**
     * Determines if the two {@code FileObject}s are equal by comparing their URIs.
     *
     * @param a the first {@code FileObject}
     * @param b the second {@code FileObject}
     * @return {@code true} if the URIs of the two {@code FileObject}s are equal
     */
    @Override
    public boolean isSameFile(FileObject a, FileObject b) {
        return a.toUri().equals(b.toUri());
    }

    @Override
    public @Nullable FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
        return Optional.ofNullable(state.getInputResource(location, packageName, relativeName))
                .orElse(delegate.getFileForInput(location, packageName, relativeName));
    }

    @Override
    public @Nullable JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) throws IOException {
        return Optional.ofNullable(state.getInputJavaFile(location, className, kind))
                .orElse(delegate.getJavaFileForInput(location, className, kind));
    }

    @Override
    public FileObject getFileForOutput(Location location, String pack, String relative, FileObject sibling) {
        return state.getOrCreateOutputResource(location, pack, relative);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String type, JavaFileObject.Kind kind, FileObject sibling) {
        return state.getOrCreateOutputJavaFile(location, type, kind);
    }


    /**
     * Returns the generated Java source files.
     *
     * @return the generated Java source files
     */
    public List<JavaFileObject> generatedSources() {
        var sources = outputFiles();
        var prefix = "/" + StandardLocation.SOURCE_OUTPUT.name();

        return sources.stream()
                .filter(it -> it.toUri().getPath().startsWith(prefix) && it.getKind() == JavaFileObject.Kind.SOURCE)
                .toList();
    }

    /**
     * Returns the output files.
     *
     * @return the output files
     */
    public List<JavaFileObject> outputFiles() {
        return new ArrayList<>(state.getOutputJavaFiles());
    }

    // ########## The following methods delegate to the standard java file manager ##########

    @Override
    public boolean contains(Location location, FileObject fo) throws IOException {
        return delegate.contains(location, fo);
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(Iterable<? extends File> files) {
        return delegate.getJavaFileObjectsFromFiles(files);
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjectsFromStrings(Iterable<String> strings) {
        return delegate.getJavaFileObjectsFromStrings(strings);
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjects(File... files) {
        return delegate.getJavaFileObjects(files);
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjects(String... strings) {
        return delegate.getJavaFileObjects(strings);
    }

    @Override
    public Iterable<? extends File> getLocation(Location location) {
        return delegate.getLocation(location);
    }

    @Override
    public Location getLocationForModule(Location location, String moduleName) throws IOException {
        return delegate.getLocationForModule(location, moduleName);
    }

    @Override
    public Location getLocationForModule(Location location, JavaFileObject file) throws IOException {
        return delegate.getLocationForModule(location, file);
    }

    @Override
    public Iterable<Set<Location>> listLocationsForModules(Location location) throws IOException {
        return delegate.listLocationsForModules(location);
    }

    @Override
    public void setLocation(Location location, Iterable<? extends File> files) throws IOException {
        delegate.setLocation(location, files);
    }

    @Override
    public void setLocationForModule(Location location, String moduleName, Collection<? extends Path> paths) throws IOException {
        delegate.setLocationForModule(location, moduleName, paths);
    }

    @Override
    public void setLocationFromPaths(Location location, Collection<? extends Path> paths) throws IOException {
        delegate.setLocationFromPaths(location, paths);
    }

    @Override
    public void setPathFactory(PathFactory factory) {
        delegate.setPathFactory(factory);
    }

    @Override
    public boolean hasLocation(Location location) {
        return delegate.hasLocation(location);
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        return delegate.getClassLoader(location);
    }

    @Override
    public <S> ServiceLoader<S> getServiceLoader(Location location, Class<S> service) throws IOException {
        return delegate.getServiceLoader(location, service);
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String pack, Set<JavaFileObject.Kind> kinds, boolean recursive) throws IOException {
        return delegate.list(location, pack, kinds, recursive);
    }

    @Override
    public Path asPath(FileObject file) {
        return delegate.asPath(file);
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        return delegate.inferBinaryName(location, file);
    }

    @Override
    public String inferModuleName(Location location) throws IOException {
        return delegate.inferModuleName(location);
    }

    @Override
    public boolean handleOption(String current, Iterator<String> remaining) {
        return delegate.handleOption(current, remaining);
    }

    @Override
    public int isSupportedOption(String option) {
        return delegate.isSupportedOption(option);
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
