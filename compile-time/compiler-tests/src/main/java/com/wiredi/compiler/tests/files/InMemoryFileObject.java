package com.wiredi.compiler.tests.files;

import javax.tools.FileObject;
import java.io.*;
import java.net.URI;
import java.time.Instant;

public class InMemoryFileObject implements FileObject {

    private final URI uri;
    private String content;
    private long lastModified = Instant.now().toEpochMilli();

    public InMemoryFileObject(URI uri, byte[] bytes) {
        this.uri = uri;
        this.content = new String(bytes);
    }

    public InMemoryFileObject(URI uri, String content) {
        this.uri = uri;
        this.content = content;
    }

    public InMemoryFileObject(URI uri) {
        this.uri = uri;
    }

    @Override
    public URI toUri() {
        return uri;
    }

    @Override
    public String getName() {
        return URIs.getFileName(uri);
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
                lastModified = Instant.now().toEpochMilli();
            }
        };
    }

    @Override
    public Writer openWriter() {
        return new StringWriter() {
            @Override
            public void close() {
                content = toString();
                lastModified = Instant.now().toEpochMilli();
            }
        };
    }

    @Override
    public long getLastModified() {
        return lastModified;
    }

    @Override
    public boolean delete() {
        content = null;
        return true;
    }
}
