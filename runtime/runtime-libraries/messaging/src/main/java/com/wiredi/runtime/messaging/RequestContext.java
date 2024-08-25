package com.wiredi.runtime.messaging;

import com.wiredi.runtime.lang.ThrowingRunnable;
import com.wiredi.runtime.lang.ThrowingSupplier;

import java.util.List;

/**
 * A request context aggregates a list of {@link RequestAware} instances, allowing for easy use of callbacks during
 * the handling of Messages.
 * <p>
 * Systems that use the Messaging module can use this context to work on messages.
 * This way it is possible to write generic interceptors of any request sequence.
 * <p>
 * By that, we achieve a generic request context for any kind of messaging integration.
 * Imagine this as a general abstraction which can be used in the WebContext and in the KafkaContext.
 * Implementations of the {@link RequestAware} might consider the {@link MessageDetails} to determine the kind of
 * request that is being processed.
 * <p>
 * Additionally, this context allows for easy header access by setting the {@link Headers} in the {@link MessageHeadersAccessor}.
 * <p>
 * Whenever you implement any kind of request processing that uses the messaging api for (de)serialization, consider
 * using this context when processing the message.
 * <p>
 * The {@link RequestContext}
 *
 * @see RequestAware
 * @see MessageHeadersAccessor
 */
public class RequestContext {

    private final List<RequestAware> requestAwareList;
    private final MessageHeadersAccessor headersAccessor;

    public RequestContext(
            List<RequestAware> requestAwareList,
            MessageHeadersAccessor headersAccessor
    ) {
        this.requestAwareList = requestAwareList;
        this.headersAccessor = headersAccessor;
    }

    /**
     * Executes the {@code throwingRunnable} in the context of the Message.
     * <p>
     * It uses the {@link MessageHeadersAccessor} to make headers available.
     * <p>
     * Additionally, any available {@link RequestAware} will be invoked.
     *
     * @param message The message that is being processed
     * @param throwingRunnable The process that processes the message
     * @param <E> Any exception that can occur
     * @throws E The exception, if the {@link ThrowingRunnable} throws the exception.
     */
    public <E extends Throwable> void execute(Message<?, ?> message, ThrowingRunnable<E> throwingRunnable) throws E {
        Headers previous = headersAccessor.set(message.getHeaders());
        try {
            requestAwareList.forEach(it -> it.started(message));
            throwingRunnable.run();
            requestAwareList.forEach(requestAware -> requestAware.successful(message));
        } catch (Throwable throwable) {
            requestAwareList.forEach(requestAware -> requestAware.failed(throwable, message));
            throw throwable;
        } finally {
            try {
                requestAwareList.forEach(requestAware -> requestAware.completed(message));
            } finally {
                headersAccessor.set(previous);
            }
        }
    }

    /**
     * Executes the {@code throwingSupplier} in the context of the Message and returns its result.
     * <p>
     * It uses the {@link MessageHeadersAccessor} to make headers available.
     * <p>
     * Additionally, any available {@link RequestAware} will be invoked.
     *
     * @param message The message that is being processed
     * @param throwingSupplier The process that processes the message
     * @param <E> Any exception that can occur
     * @throws E The exception, if the {@link ThrowingRunnable} throws the exception.
     */
    public <T, E extends Throwable> T getAndGet(Message<?, ?> message, ThrowingSupplier<T, E> throwingSupplier) throws E {
        Headers previous = headersAccessor.set(message.getHeaders());
        try {
            requestAwareList.forEach(it -> it.started(message));
            T t = throwingSupplier.get();
            requestAwareList.forEach(requestAware -> requestAware.successful(message));
            return t;
        } catch (Throwable throwable) {
            requestAwareList.forEach(requestAware -> requestAware.failed(throwable, message));
            throw throwable;
        } finally {
            try {
                requestAwareList.forEach(requestAware -> requestAware.completed(message));
            } finally {
                headersAccessor.set(previous);
            }
        }
    }
}
