package com.wiredi.compiler.processor.metainf;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MetaInf {

    public static final String META_INF_PATH = "META-INF";
    private final Filer filer;

    public MetaInf(Filer filer) {
        this.filer = filer;
    }

    private static String join(String... segments) {
        return Arrays.stream(segments)
                .filter(Objects::nonNull)
                .map(s -> s.replaceAll("^/+", "").replaceAll("/+$", ""))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("/"));
    }

    public MetaInfFile access(String fileName) throws IOException {
        FileObject fileObject = getOrCreateFile(fileName);
        return new MetaInfFile(fileObject, readFile(fileObject.openInputStream()));
    }

    public Collection<String> readFile(InputStream input) throws IOException {
        Collection<String> serviceClasses = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int commentStart = line.indexOf('#');
                if (commentStart >= 0) {
                    line = line.substring(0, commentStart);
                }
                line = line.trim();
                if (!line.isEmpty()) {
                    serviceClasses.add(line);
                }
            }
            return serviceClasses;
        }
    }

    public void writeFile(
            Collection<String> lines,
            String fileName
    ) throws IOException {
        writeFile(lines, fileName, FileOperations.ALLOW_DUPLICATE_LINES);
    }

    public void writeFile(
            Collection<String> lines,
            String fileName,
            FileOperations... operations
    ) throws IOException {
        FileObject fileObject = getOrCreateFile(fileName);
        List<FileOperations> fileOperations = Arrays.asList(operations);
        List<String> existingLines = new ArrayList<>(lines);

        if (!fileOperations.contains(FileOperations.OVERWRITE_CONTENT)) {
            try (InputStream inputStream = fileObject.openInputStream()) {
                existingLines.addAll(readFile(inputStream));
            }
        }

        if (!fileOperations.contains(FileOperations.ALLOW_DUPLICATE_LINES)) {
            HashSet<String> uniqueLines = new HashSet<>(existingLines);
            existingLines.clear();
            existingLines.addAll(uniqueLines);
        }

        flush(fileObject, existingLines);
    }

    private void flush(FileObject fileObject, List<String> lines) throws IOException {
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

    private FileObject getOrCreateFile(String fileName) throws IOException {
        String path = join(META_INF_PATH, fileName);
        try {
            return filer.getResource(StandardLocation.CLASS_OUTPUT, "", path);
        } catch (IOException e) {
            return filer.createResource(StandardLocation.CLASS_OUTPUT, "", path);
        }
    }
}
