package com.wiredi.runtime.messaging;

import com.wiredi.runtime.messaging.compression.MessageCompression;

public interface MessageCompressionConfiguration {

    void configure(MessageCompression messageCompression);

}
