package com.wiredi.compiler.tests.files;

import org.jetbrains.annotations.NotNull;

import javax.tools.JavaFileObject;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class URIs {

    /**
     * Creates a URI from the path portion of the given URL if it represents a
     * resource in a JAR.
     *
     * @param resource the resource
     * @return the URI
     * @throws IllegalStateException if this URL is not formatted strictly according to
     *                            to RFC2396 and cannot be converted to a URI.
     */
    public static URI create(URL resource) {
        if (!resource.getProtocol().equals("jar")) {
            try {
                return resource.toURI();
            } catch (URISyntaxException e) {
                throw new IllegalStateException(e);
            }
        }

        return URI.create(resource.getPath().split("!")[1]);
    }

    public static URL toURL(URI uri) {
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Deduces the file extension of the given URI.
     *
     * @param uri the URI
     * @return the file kind that the given URI represents
     */
    @NotNull
    public static JavaFileObject.Kind deduceJavaFileKind(URI uri) {
        var path = uri.getPath();
        for (var kind : JavaFileObject.Kind.values()) {
            if (path.endsWith(kind.extension)) {
                return kind;
            }
        }

        throw new IllegalArgumentException("Could not deduce java file kind from " + uri);
    }

    public static String getFileName(URI uri) {
        String[] path = uri.toString().split("/");
        return path[path.length - 1];
    }
}
