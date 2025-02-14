package com.wiredi.runtime.messaging;

import com.wiredi.runtime.lang.ThrowingSupplier;
import com.wiredi.runtime.messaging.compression.MessageCompression;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class defines how a request is to be handled.
 * It aggregates other classes that should be invoked during handling of the {@link Message}.
 * <p>
 * This class is primarily used by the {@link MessagingEngine}, which constructs the {@link Message} and handles it.
 * However, Systems that use the Messaging integration can use this context to manually run work on messages.
 * This way it is possible to write generic interceptors of any request sequence.
 * <p>
 * By that, we achieve a generic request context for any kind of messaging integration.
 * Imagine this as a general abstraction which can be used in the WebContext and in the KafkaContext.
 * Implementations of the {@link RequestAware} might consider the {@link MessageDetails} to determine the kind of
 * request that is being processed.
 * <p>
 * The {@link MessageFilter} provided to the engine may skip messages and stop processing early.
 * <p>
 * Additionally, this context allows for easy header access by setting the {@link MessageHeaders} in the {@link MessageHeadersAccessor}.
 * <p>
 * Whenever you implement any kind of request processing that uses the messaging api for (de)serialization, consider
 * using this context when processing the message.
 * <p>
 * The {@link RequestContext}
 *
 * @see RequestAware
 * @see MessageFilter
 * @see MessagingErrorHandler
 * @see MessageHeadersAccessor
 */
public class RequestContext {

    private static final RequestContext GLOBAL_INSTANCE = empty();
    private final List<RequestAware> requestAwareList;
    private final List<MessageFilter> messageFilters;
    private final MessagingErrorHandler messagingErrorHandler;
    private final MessageHeadersAccessor headersAccessor;
    private final MessageCompression messageCompression;

    public RequestContext(
            List<RequestAware> requestAwareList,
            List<MessageFilter> messageFilters,
            MessagingErrorHandler messagingErrorHandler,
            MessageHeadersAccessor headersAccessor,
            MessageCompression messageCompression
    ) {
        this.requestAwareList = requestAwareList;
        this.messageFilters = messageFilters;
        this.messagingErrorHandler = messagingErrorHandler;
        this.headersAccessor = headersAccessor;
        this.messageCompression = messageCompression;
    }

    public static RequestContext defaultInstance() {
        return GLOBAL_INSTANCE;
    }

    public static RequestContext empty() {
        return new RequestContext(
                new ArrayList<>(),
                new ArrayList<>(),
                MessagingErrorHandler.DEFAULT,
                new MessageHeadersAccessor(),
                MessageCompression.newDefault()
        );
    }

    public List<RequestAware> requestAwareListeners() {
        return requestAwareList;
    }

    public List<MessageFilter> messageFilters() {
        return messageFilters;
    }

    public MessagingErrorHandler messagingErrorHandler() {
        return messagingErrorHandler;
    }

    public MessageHeadersAccessor headersAccessor() {
        return headersAccessor;
    }

    /**
     * Executes the {@code throwingSupplier} in the context of the Message and returns its result.
     * <p>
     * This method:
     * <ul>
     *     <li>Set up the {@link MessageHeadersAccessor} to make the headers accessible</li>
     *     <li>Ignore messages for which a {@link MessageFilter} filters a message</li>
     *     <li>Invokes all {@link RequestAware} instances</li>
     *     <li>Uses the {@link MessagingErrorHandler} to handle errors</li>
     * </ul>
     * <p>
     * The {@link MessagingErrorHandler} will be invoked for any exception raised during the process, with the following
     * exceptions:
     * <ul>
     *     <li>Errors raised during {@link RequestAware#completed(Message)} will be directly propagated</li>
     *     <li>Errors thrown during {@link MessagingErrorHandler#handleError(Message, Throwable)}</li>
     * </ul>
     *
     * @param message          The message that is being processed
     * @param throwingSupplier The process that processes the message
     * @param <E>              Any exception that can occur
     */
    public <T, E extends Throwable> MessagingResult handleRequest(Message<?> message, ThrowingSupplier<T, E> throwingSupplier) {
        AtomicReference<Message<?>> messagePointer = new AtomicReference<>(messageCompression.decompress(message));
        try {
            // First step: prepare the message
            for (RequestAware requestAware : requestAwareList) {
                messagePointer.set(requestAware.started(messagePointer.get()));
            }

            Message<?> targetMessage = messagePointer.get();
            // Seconds step: Try to filter the message
            if (messageFilters.stream().anyMatch(it -> it.shouldSkip(targetMessage))) {
                return new MessagingResult.SkipMessage();
            }

            // Third step: Now invoke the actual runnable
            T result = headersAccessor.getWith(targetMessage.headers(), throwingSupplier);

            // Fourth step: Notify successful invocation
            requestAwareList.forEach(requestAware -> requestAware.successful(targetMessage));
            return new MessagingResult.Success(result);
        } catch (Throwable throwable) {
            requestAwareList.forEach(requestAware -> {
                try {
                    requestAware.failed(messagePointer.get(), throwable);
                } catch (Throwable t2) {
                    throwable.addSuppressed(t2);
                }
            });
            return messagingErrorHandler.handleError(messagePointer.get(), throwable);
        } finally {
            // Fifth step: Notify about completion
            requestAwareList.forEach(requestAware -> requestAware.completed(messagePointer.get()));
        }
    }
}
