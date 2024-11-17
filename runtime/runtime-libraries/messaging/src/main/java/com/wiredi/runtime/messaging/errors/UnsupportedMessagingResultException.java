package com.wiredi.runtime.messaging.errors;

import com.wiredi.runtime.messaging.MessagingResult;

public class UnsupportedMessagingResultException extends MessagingException {
    private final MessagingResult messagingResult;

    public UnsupportedMessagingResultException(MessagingResult messagingResult) {
        this(messagingResult, "The following MessagingResults is currently not supported: " + messagingResult.toString());
    }
    public UnsupportedMessagingResultException(MessagingResult messagingResult, String message) {
        super(message);
        this.messagingResult = messagingResult;
    }

    public MessagingResult getMessagingResult() {
        return messagingResult;
    }
}
