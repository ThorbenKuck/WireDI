package com.wiredi.runtime.messaging.compression;

import com.wiredi.runtime.messaging.Message;
import com.wiredi.runtime.messaging.MessageDetails;
import com.wiredi.runtime.messaging.MessageHeader;
import com.wiredi.runtime.messaging.errors.MissingMessageCompressionAlgorithm;
import com.wiredi.runtime.messaging.errors.NoMatchingMessageCompressionAlgorithm;
import com.wiredi.runtime.messaging.messages.SimpleMessage;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MessageCompression {

    private static final List<String> KNOWN_COMPRESSION_HEADERS = List.of("Content-Encoding");
    private final Map<String, MessageCompressionAlgorithm> algorithms = new HashMap<>();
    private final List<String> compressionAlgorithmHeaders = new ArrayList<>(KNOWN_COMPRESSION_HEADERS);

    public MessageCompression(List<MessageCompressionAlgorithm> messageCompressionAlgorithms) {
        messageCompressionAlgorithms.forEach(this::register);
    }

    public static MessageCompression newDefault() {
        return new MessageCompression(List.of(new GzipMessageCompressionAlgorithm()));
    }

    public synchronized void register(MessageCompressionAlgorithm algorithm) {
        for (String identifier : algorithm.identifiers()) {
            MessageCompressionAlgorithm messageCompressionAlgorithm = algorithms.get(identifier);
            if (messageCompressionAlgorithm != null) {
                throw new IllegalArgumentException("Duplicate identifier " + identifier + ". Found the already registered compression algorithm " + messageCompressionAlgorithm + " whilst registering " + algorithm);
            }
        }

        algorithm.identifiers().forEach(identifier -> algorithms.put(identifier, algorithm));
    }

    public void addCompressionHeaderName(String name) {
        this.compressionAlgorithmHeaders.add(name);
    }

    public void removeCompressionHeaderName(String name) {
        this.compressionAlgorithmHeaders.remove(name);
    }

    public byte[] decompress(byte[] bytes, List<Algorithm> algorithms) {
        return tryDecompress(bytes, algorithms).orElseThrow(() -> {
            if (algorithms.size() == 1) {
                return new MissingMessageCompressionAlgorithm(algorithms.getFirst());
            } else {
                return new NoMatchingMessageCompressionAlgorithm(algorithms);
            }
        });
    }

    @NotNull
    public <T extends MessageDetails> Message<T> decompress(Message<T> message) {
        for (String header : this.compressionAlgorithmHeaders) {
            Algorithm algorithm = Algorithm.inOrder(message.headers(header).stream().map(MessageHeader::decodeToString).toList());
            Optional<Message<T>> compressedMessage = decompress(message.body(), algorithm).map(body -> Message.builder(body)
                    .withDetails(message.details())
                    .addHeaders(message.headers())
                    .build());

            if (compressedMessage.isPresent()) {
                return compressedMessage.get();
            }
        }

        return message;
    }

    @NotNull
    public Optional<byte[]> tryDecompress(byte[] bytes, List<Algorithm> algorithms) {
        if (algorithms.isEmpty()) {
            return Optional.of(bytes);
        }

        for (Algorithm algorithm : algorithms) {
            Optional<byte[]> decompressed = decompress(bytes, algorithm);
            if (decompressed.isPresent()) {
                return decompressed;
            }
        }

        return Optional.empty();
    }

    private Optional<byte[]> decompress(byte[] bytes, Algorithm algorithm) {
        byte[] result = bytes;

        for (String identifier : algorithm.identifiers) {
            MessageCompressionAlgorithm compressionAlgorithm = this.algorithms.get(identifier);
            if (compressionAlgorithm != null) {
                result = compressionAlgorithm.decompress(bytes);
            } else {
                return Optional.empty();
            }
        }

        return Optional.of(result);
    }

    public byte[] compress(byte[] bytes, List<Algorithm> algorithms) {
        return tryCompress(bytes, algorithms).orElseThrow(() -> {
            if (algorithms.size() == 1) {
                return new MissingMessageCompressionAlgorithm(algorithms.getFirst());
            } else {
                return new NoMatchingMessageCompressionAlgorithm(algorithms);
            }
        });
    }

    @NotNull
    public <T extends MessageDetails> Message<T> compress(Message<T> message) {
        for (String header : this.compressionAlgorithmHeaders) {
            Algorithm algorithm = Algorithm.inOrder(message.headers(header).stream().map(MessageHeader::decodeToString).toList());
            Optional<Message<T>> compressedMessage = compress(message.body(), algorithm).map(body -> Message.builder(body)
                    .withDetails(message.details())
                    .addHeaders(message.headers())
                    .build());

            if (compressedMessage.isPresent()) {
                return compressedMessage.get();
            }
        }

        return message;
    }

    @NotNull
    public Optional<byte[]> tryCompress(byte[] bytes, List<Algorithm> algorithms) {
        if (algorithms.isEmpty()) {
            return Optional.of(bytes);
        }

        for (Algorithm algorithm : algorithms) {
            Optional<byte[]> compressed = compress(bytes, algorithm);
            if (compressed.isPresent()) {
                return compressed;
            }
        }

        return Optional.empty();
    }

    private Optional<byte[]> compress(byte[] bytes, Algorithm algorithm) {
        byte[] result = bytes;

        for (String identifier : algorithm.identifiers) {
            MessageCompressionAlgorithm compressionAlgorithm = this.algorithms.get(identifier);
            if (compressionAlgorithm != null) {
                result = compressionAlgorithm.compress(bytes);
            } else {
                return Optional.empty();
            }
        }

        return Optional.of(result);
    }
}
