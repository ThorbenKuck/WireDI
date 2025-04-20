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

    public <T extends MessageDetails> Message<T> decompress(Message<T> message, List<Algorithm> algorithms) {
        return tryDecompress(message, algorithms).orElseThrow(() -> {
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
            Optional<Message<T>> decompressedMessage = decompress(message, algorithm);

            if (decompressedMessage.isPresent()) {
                return decompressedMessage.get();
            }
        }

        return message;
    }

    @NotNull
    public <T extends MessageDetails> Optional<Message<T>> tryDecompress(Message<T> bytes, List<Algorithm> algorithms) {
        if (algorithms.isEmpty()) {
            return Optional.of(bytes);
        }

        for (Algorithm algorithm : algorithms) {
            Optional<Message<T>> decompressed = decompress(bytes, algorithm);
            if (decompressed.isPresent()) {
                return decompressed;
            }
        }

        return Optional.empty();
    }

    private <T extends MessageDetails> Optional<Message<T>> decompress(Message<T> message, Algorithm algorithm) {
        Message<T> result = message;

        for (String identifier : algorithm.identifiers) {
            MessageCompressionAlgorithm compressionAlgorithm = this.algorithms.get(identifier);
            if (compressionAlgorithm != null) {
                result = compressionAlgorithm.decompress(result);
            } else {
                return Optional.empty();
            }
        }

        return Optional.of(result);
    }

    public <T extends MessageDetails> Message<T> compress(Message<T> bytes, List<Algorithm> algorithms) {
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
            Optional<Message<T>> compressedMessage = compress(message, algorithm);

            if (compressedMessage.isPresent()) {
                return compressedMessage.get();
            }
        }

        return message;
    }

    @NotNull
    public <T extends MessageDetails> Optional<Message<T>> tryCompress(Message<T> bytes, List<Algorithm> algorithms) {
        if (algorithms.isEmpty()) {
            return Optional.of(bytes);
        }

        for (Algorithm algorithm : algorithms) {
            Optional<Message<T>> compressed = compress(bytes, algorithm);
            if (compressed.isPresent()) {
                return compressed;
            }
        }

        return Optional.empty();
    }

    private <T extends MessageDetails> Optional<Message<T>> compress(Message<T> bytes, Algorithm algorithm) {
        Message<T> result = bytes;

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
