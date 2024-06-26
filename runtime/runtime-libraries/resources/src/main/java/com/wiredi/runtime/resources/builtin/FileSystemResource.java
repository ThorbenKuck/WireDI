package com.wiredi.runtime.resources.builtin;

import com.wiredi.runtime.resources.PathUtils;
import com.wiredi.runtime.resources.Resource;
import com.wiredi.runtime.resources.WritableResource;
import com.wiredi.runtime.resources.exceptions.ResourceException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileSystemResource implements WritableResource {

    @NotNull
    private final String rawPath;
    @NotNull
    private final Path path;

    public FileSystemResource(String... path) {
        this(Path.of(PathUtils.join(path)));
    }

    public FileSystemResource(Path path) {
        this.rawPath = path.toAbsolutePath().toString();
        this.path = path;
    }

    @Override
    public boolean exists() {
        return Files.exists(path);
    }

    @Override
    public boolean isFile() {
        return Files.isRegularFile(path);
    }

    @Override
    @NotNull
    public URL getURL() {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new ResourceException("Error creating URL", e);
        }
    }

    @Override
    @NotNull
    public URI getURI() {
        return path.toUri();
    }

    @Override
    @NotNull
    public Path getPath() {
        return path;
    }

    @Override
    @NotNull
    public InputStream getInputStream() {
        try {
            return Files.newInputStream(path.toAbsolutePath());
        } catch (@NotNull final IOException e) {
            throw new ResourceException("Error opening input stream", e);
        }
    }

    @Override
    @NotNull
    public Resource createRelative(@NotNull final String relativePath) {
        return new FileSystemResource(path.resolve(relativePath));
    }

    @Override
    @NotNull
    public String getFilename() {
        return rawPath;
    }

    @Override
    public boolean isWritable() {
        return Files.isWritable(path);
    }

    @Override
    public String toString() {
        return "FileSystemResource(" + rawPath + ")";
    }

    @Override
    @NotNull
    public OutputStream getOutputStream() throws IOException {
        return Files.newOutputStream(path);
    }
}
