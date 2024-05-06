package com.wiredi.compiler.tests.files.utils;

import org.jetbrains.annotations.Nullable;

import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import java.util.List;

public class FileObjectFactory {

    private final JavaFileObjectFactory javaFileObjectFactory;
    private final ResourceFileObjectFactory resourceFileObjectFactory;

    public FileObjectFactory(@Nullable String rootFolder) {
        this.javaFileObjectFactory = new JavaFileObjectFactory(rootFolder);
        this.resourceFileObjectFactory = new ResourceFileObjectFactory(rootFolder);
    }

    public FileObjectFactory() {
        this(null);
    }

    public JavaFileObjectFactory javaFileObjectFactory() {
        return javaFileObjectFactory;
    }

    public ResourceFileObjectFactory resourceFileObjectFactory() {
        return resourceFileObjectFactory;
    }

    public void setRootFolder(String rootFolder) {
        javaFileObjectFactory.setRootFolder(rootFolder);
        resourceFileObjectFactory.setRootFolder(rootFolder);
    }

    public JavaFileObject loadClass(String name) {
        return javaFileObjectFactory.load(name);
    }

    public List<JavaFileObject> loadClassesInFolder(String name) {
        return javaFileObjectFactory.loadFromFolder(name);
    }

    public FileObject loadResource(String name) {
        return resourceFileObjectFactory.load(name);
    }
}
