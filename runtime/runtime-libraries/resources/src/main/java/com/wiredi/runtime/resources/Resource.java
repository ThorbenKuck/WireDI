package com.wiredi.runtime.resources;

import com.wiredi.runtime.resources.exceptions.ResourceException;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A Resource is an object representation and a pointer to a concrete Resource.
 * <p>
 * Implementations of this interface can define how the resource is accessed.
 * <p>
 * Implementations must handle the case, that the Resource does not Exist.
 */
public interface Resource {

    /**
     * Whether the resource that this instance is pointing to exists.
     *
     * @return true if the represented resource exists.
     */
    boolean exists();

    /**
     * Whether the resource that this instance is pointing to is a file.
     *
     * @return true, if the represented resource is a file
     */
    boolean isFile();

    /**
     * Constructs a new URL for the Resource.
     * <p>
     * If the Resource does not exist, or a URL could not be constructed,
     * implementations should throw a {@link ResourceException}.
     *
     * @return a new URL for this Resource
     * @see ResourceException
     */
    @NotNull
    URL getURL();

    /**
     * Constructs a new URI for the Resource.
     * <p>
     * If the Resource does not exist, or a URI could not be constructed,
     * implementations should throw a {@link ResourceException}.
     *
     * @return a new URI for this Resource
     * @see ResourceException
     */
    @NotNull
    URI getURI();

    /**
     * Constructs a new Path for the Resource.
     * <p>
     * If the Resource does not exist, or a Path could not be constructed,
     * implementations should throw a {@link ResourceException}.
     *
     * @return a new Path for this Resource
     * @see ResourceException
     */
    @NotNull
    Path getPath();

    /**
     * Constructs a new InputStream for the Resource.
     * <p>
     * If the Resource does not exist, or a InputStream could not be constructed,
     * implementations should throw a {@link ResourceException}.
     *
     * @return a new InputStream for this Resource
     * @see ResourceException
     */
    @NotNull
    InputStream getInputStream();

    /**
     * Create a new Resource, relative to the current one.
     * <p>
     * If the current Resource does not exist, or a InputStream could not be constructed,
     * implementations should throw a {@link ResourceException}.
     * <p>
     * Most notably, if the root resource is not a directory, it should throw an exception
     *
     * @param relativePath the path, relative to which it should resolve
     * @return A resource, pointing to the path relative to the root resource.
     */
    @NotNull
    Resource createRelative(@NotNull final String relativePath);

    /**
     * Returns the name of the file.
     * <p>
     * The file name is not required to be only the file. It can be the whole path, or only the file name.
     * The details of this depend on the implementation.
     *
     * @return the file name.
     */
    String getFilename();

    /**
     * A utility function to consume this resource if it exists.
     *
     * @param resourceConsumer a consumer for the resource.
     */
    default void ifExists(Consumer<Resource> resourceConsumer) {
        if (exists()) {
            resourceConsumer.accept(this);
        }
    }

    default Optional<String> fileType() {
        String filename = getFilename();
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }


    default byte[] getContentAsByteArray() throws IOException {
        return getInputStream().readAllBytes();
    }

    default String getContentAsString() {
        return getContentAsString(Charset.defaultCharset());
    }

    default String getContentAsString(Charset charset) {
        try {
            return new String(getInputStream().readAllBytes(), charset);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    default Reader openReader() {
        return new InputStreamReader(getInputStream());
    }

    default boolean doesNotExist() {
        return !exists();
    }
}
