package com.wiredi.runtime.messaging.compression;

import com.wiredi.runtime.messaging.Message;
import com.wiredi.runtime.messaging.MessageDetails;

import java.util.List;

public interface MessageCompressionAlgorithm {

    <T extends MessageDetails> Message<T> compress(Message<T> input);

    <T extends MessageDetails> Message<T> decompress(Message<T> input);

    List<String> identifiers();

}
