package com.wiredi.compiler.metainf;

import javax.tools.FileObject;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MetaInfFile implements AutoCloseable {

    private final List<String> lines;
    private final FileObject fileObject;

    public MetaInfFile(FileObject fileObject, Collection<String> lines) {
        this.fileObject = fileObject;
        this.lines = new ArrayList<>(lines);
    }

    public void setLine(String line) {
        lines.clear();
        appendLine(line);
    }

    public void setLines(Collection<String> line) {
        lines.clear();
        appendLines(line);
    }

    public void appendLine(String line) {
        lines.add(line);
    }

    public void appendLines(Collection<String> line) {
        lines.addAll(line);
    }

    @Override
    public void close() throws Exception {
        flush();
    }

    public InputStream inputStream() throws IOException {
        return fileObject.openInputStream();
    }

    public void clear() {
        this.lines.clear();
    }

    public void flush() throws IOException {
        try (
                OutputStream outputStream = fileObject.openOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, UTF_8))
        ) {
            for (String service : lines) {
                writer.write(service);
                writer.newLine();
            }
            writer.flush();
        }
    }
}
