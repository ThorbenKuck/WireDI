package com.wiredi.compiler.tests.files;

import javax.tools.SimpleJavaFileObject;
import java.io.*;
import java.net.URI;
import java.time.Instant;

public class InMemoryJavaFile extends SimpleJavaFileObject {

    private final long timestamp = Instant.now().toEpochMilli();
    private String content;


    /**
     * Construct a SimpleJavaFileObject of the given kind and with the
     * given URI.
     *
     * @param uri  the URI for this file object
     * @param kind the kind of this file object
     */
    public InMemoryJavaFile(URI uri, Kind kind, byte[] bytes) {
        super(uri, kind);
        this.content = new String(bytes);
    }


    /**
     * Construct a SimpleJavaFileObject of the given kind and with the
     * given URI.
     *
     * @param uri  the URI for this file object
     * @param kind the kind of this file object
     */
    public InMemoryJavaFile(URI uri, Kind kind, String content) {
        super(uri, kind);
        this.content = content;
    }

    public InMemoryJavaFile(URI uri) {
        super(uri, URIs.deduceJavaFileKind(uri));
    }

    @Override
    public InputStream openInputStream() {
        return new ByteArrayInputStream(content.getBytes());
    }

    @Override
    public Reader openReader(boolean ignoreEncodingErrors) {
        return new StringReader(content);
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return content;
    }

    @Override
    public OutputStream openOutputStream() {
        return new ByteArrayOutputStream() {
            @Override
            public void close() {
                content = new String(toByteArray());
            }
        };
    }

    @Override
    public Writer openWriter() {
        return new StringWriter() {
            @Override
            public void close() {
                content = toString();
            }
        };
    }

    @Override
    public boolean delete() {
        content = null;
        return true;
    }

    @Override
    public long getLastModified() {
        return timestamp;
    }
}
