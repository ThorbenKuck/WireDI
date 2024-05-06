package com.wiredi.compiler.tests;

import com.wiredi.compiler.tests.files.utils.FileObjectFactory;
import com.wiredi.compiler.tests.files.InMemoryJavaFileManager;
import com.wiredi.compiler.tests.files.FileManagerState;
import com.wiredi.compiler.tests.result.Compilation;

import javax.annotation.processing.Processor;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URLClassLoader;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toSet;

public class Compiler {

    private static final ClassLoader PLATFORM = ClassLoader.getPlatformClassLoader();
    private static final ClassLoader APPLICATION = ClassLoader.getSystemClassLoader();
    private final JavaCompiler delegate;
    private final List<Processor> processors = new ArrayList<>();
    private final List<String> options = new ArrayList<>();
    private final Set<File> classpath = new HashSet<>();
    private final FileManagerState fileManagerState = new FileManagerState();
    private FileObjectFactory fileObjectFactory = new FileObjectFactory(null);

    /**
     * Creates a {@code Compiler} with the given underlying compiler.
     *
     * @param delegate the Java compiler
     */
    Compiler(JavaCompiler delegate) {
        this.delegate = delegate;
    }

    /**
     * Creates a Java compiler.
     *
     * @return a Java compiler
     */
    public static Compiler javac() {
        return new Compiler(ToolProvider.getSystemJavaCompiler());
    }

    public FileObjectFactory fileObjectFactory() {
        return this.fileObjectFactory;
    }

    public void setRootFolder(String rootFolder) {
        this.fileObjectFactory.setRootFolder(rootFolder);
    }

    public FileManagerState fileManagerState() {
        return fileManagerState;
    }

    public Compiler withProcessor(Processor processor) {
        processors.add(processor);
        return this;
    }

    public Compiler withProcessors(Collection<Processor> processors) {
        this.processors.addAll(processors);
        return this;
    }

    public Compiler withOption(String option) {
        this.options.add(option);
        return this;
    }

    public Compiler withClass(JavaFileObject javaFileObject) {
        this.fileManagerState.addInputJavaFile(javaFileObject);
        return this;
    }

    public Compiler withClass(String fullyQualifiedClassName) {
        return withClass(fileObjectFactory.loadClass(fullyQualifiedClassName));
    }

    public Compiler withAllClassesFromFolder(String folderName) {
        fileObjectFactory.loadClassesInFolder(folderName).forEach(this::withClass);
        return this;
    }

    /**
     * Adds the module and its transitive dependencies to the compilation classpath.
     *
     * @param module the module
     * @return {@code this}
     */
    public Compiler withModule(Module module) {
        var layer = module.getLayer();
        if (layer == null) {
            return this;
        }

        for (var resolved : layer.configuration().modules()) {
            var location = resolved.reference().location().orElseThrow(() -> new IllegalStateException("Could not find location for module: " + resolved.name()));
            classpath.add(new File(location.getPath()));
        }

        return this;
    }

    public Compiler withCurrentClasspath() {
        return withClasspath(getClass().getClassLoader());
    }

    /**
     * Adds the classpath of the given {@code ClassLoader} as the compilation classpath.
     *
     * @param loader the {@code ClassLoader} which classpath is to be used during compilation
     * @return {@code this}
     * @throws IllegalArgumentException if the given {@code ClassLoader} or its parents are neither
     *                                  {@code URLClassLoader}s nor the system/platform classloader, or if they contain a
     *                                  classpath with folders
     */
    public Compiler withClasspath(ClassLoader loader) {
        var paths = new HashSet<String>();
        while (loader != null) {
            if (loader == PLATFORM) {
                break;
            }

            if (loader == APPLICATION) {
                Collections.addAll(paths, System.getProperty("java.class.path").split(File.pathSeparator));
                break;
            }

            if (!(loader instanceof URLClassLoader)) {
                throw new IllegalArgumentException("Given ClassLoader and its parents must be a URLClassLoader");
            }

            for (var url : ((URLClassLoader) loader).getURLs()) {
                if (url.getProtocol().equals("file")) {
                    paths.add(url.getPath());

                } else {
                    throw new IllegalArgumentException("Given ClassLoader and its parents may not contain classpaths that consist of folders");
                }
            }

            loader = loader.getParent();
        }

        classpath.addAll(paths.stream().map(File::new).collect(toSet()));

        return this;
    }

    /**
     * Adds the given classpath as the compilation classpath.
     *
     * @param files the compilation classpath
     * @return {@code this}
     */
    public Compiler withClasspath(Collection<File> files) {
        classpath.addAll(files);
        return this;
    }

    /**
     * Compiles the given Java source files.
     *
     * @return the results of this compilation
     */
    public Compilation compile() {
        var diagnostics = new Diagnostics();
        var manager = new InMemoryJavaFileManager(fileManagerState, delegate.getStandardFileManager(diagnostics, Locale.getDefault(), UTF_8));

        if (classpath.isEmpty()) {
            withCurrentClasspath();
        }

        try {
            manager.setLocation(StandardLocation.CLASS_PATH, classpath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        var task = delegate.getTask(null, manager, diagnostics, options, null, fileManagerState.getInputJavaFiles());
        task.setProcessors(processors);
        var success = task.call();

        return new Compilation(fileManagerState, diagnostics, success);
    }
}
