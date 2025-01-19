package com.wiredi.runtime.messaging.compression;

import java.util.List;

public interface MessageCompressionAlgorithm {

    byte[] compress(byte[] bytes);

    byte[] decompress(byte[] bytes);

    List<String> identifiers();

}
