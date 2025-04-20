package com.wiredi.runtime.messaging.compression;

import com.wiredi.runtime.messaging.Message;
import com.wiredi.runtime.messaging.MessageDetails;

import java.io.*;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipMessageCompressionAlgorithm implements MessageCompressionAlgorithm {
    @Override
    public <T extends MessageDetails> Message<T> compress(Message<T> message) {
        try (
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final GZIPOutputStream gzos = new GZIPOutputStream(baos)
        ) {
            gzos.write(message.body());
            return message.copyWithPayload(baos.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public <T extends MessageDetails> Message<T> decompress(Message<T> bytes) {
        try (final InputStream inputStream = new GZIPInputStream(bytes.inputStream())) {
            return bytes.copyWithPayload(inputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public List<String> identifiers() {
        return List.of("gzip");
    }
}
