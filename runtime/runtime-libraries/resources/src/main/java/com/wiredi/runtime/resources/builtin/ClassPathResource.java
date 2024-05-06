package com.wiredi.runtime.resources.builtin;

import com.wiredi.runtime.resources.PathUtils;
import com.wiredi.runtime.resources.Resource;
import com.wiredi.runtime.resources.exceptions.ResourceException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.HashMap;

public class ClassPathResource implements Resource {

    private final String path;
    private final ClassLoader classLoader;

    public ClassPathResource(@NotNull String path) {
        this(path, Thread.currentThread().getContextClassLoader());
    }

    public ClassPathResource(@NotNull String path, @Nullable ClassLoader classLoader) {
        this.path = path;
        this.classLoader = classLoader;
    }

    @Override
    public boolean exists() {
        return this.classLoader.getResource(this.path) != null && (resolveUrl() != null);
    }

    @Override
    public boolean isFile() {
        return Files.isRegularFile(getPath());
    }

    @Override
    public @NotNull URL getURL() {
        URL url = resolveUrl();
        if (url == null) {
            throw new ResourceException("Could not create an URL for the resource " + path);
        }
        return url;
    }

    @Override
    public @NotNull URI getURI() {
        try {
            return getURL().toURI();
        } catch (URISyntaxException e) {
            throw new ResourceException("Error creating URI", e);
        }
    }

    @Override
    public @NotNull Path getPath() {
        URI uri = getURI();
        try {
            if (uri.toString().contains("!")) {
                String[] split = uri.toString().split("!");
                try {
                    return FileSystems.getFileSystem(URI.create(split[0])).getPath(split[1]);
                } catch (ProviderNotFoundException t) {
                    try (FileSystem fs = FileSystems.newFileSystem(URI.create(split[0]), new HashMap<>())) {
                        return fs.getPath(split[1]);
                    }
                }
            } else {
                return Paths.get(uri);
            }
        } catch (FileSystemNotFoundException e) {
            throw new IllegalStateException("Error while parsing uri " + uri, e);
        } catch (IOException e) {
            throw new IllegalStateException("Error while constructing a file system for " + uri, e);
        }
    }

    @Override
    public @NotNull InputStream getInputStream() {
        InputStream is;
        if (this.classLoader != null) {
            is = this.classLoader.getResourceAsStream(this.path);
        } else {
            is = ClassLoader.getSystemResourceAsStream(this.path);
        }
        if (is == null) {
            throw new ResourceException(path + " cannot be opened because it does not exist");
        }
        return is;
    }

    @Override
    public @NotNull Resource createRelative(@NotNull String relativePath) {
        if (isFile()) {
            throw new ResourceException("Cannot create a relative path from file " + path);
        }
        return new ClassPathResource(PathUtils.concat(path, relativePath));
    }

    @Override
    public String getFilename() {
        return path;
    }

    @Override
    public String toString() {
        return "ClassPathResource(" + path + ")";
    }

    private URL resolveUrl() {
        if (this.classLoader != null) {
            return this.classLoader.getResource(this.path);
        } else {
            return ClassLoader.getSystemResource(this.path);
        }
    }
}
