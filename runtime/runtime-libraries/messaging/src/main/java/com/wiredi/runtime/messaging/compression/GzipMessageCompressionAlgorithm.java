package com.wiredi.runtime.messaging.compression;

import java.io.*;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipMessageCompressionAlgorithm implements MessageCompressionAlgorithm {
    @Override
    public byte[] compress(byte[] bytes) {
        try (
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final GZIPOutputStream gzos = new GZIPOutputStream(baos)
        ) {
            gzos.write(bytes);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        try (final InputStream inputStream = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public List<String> identifiers() {
        return List.of("gzip");
    }
}
