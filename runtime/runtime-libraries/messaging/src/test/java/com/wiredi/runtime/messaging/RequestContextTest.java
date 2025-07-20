package com.wiredi.runtime.messaging;

import com.wiredi.runtime.messaging.compression.MessageCompression;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestContextTest {

    @Test
    void testDefaultInstance() {
        // Act
        RequestContext context = RequestContext.defaultInstance();

        // Assert
        assertThat(context).isNotNull();
        assertThat(context.requestAwareListeners()).isEmpty();
        assertThat(context.messageFilters()).isEmpty();
        assertThat(context.messagingErrorHandler()).isEqualTo(MessagingErrorHandler.DEFAULT);
        assertThat(context.headersAccessor()).isNotNull();
    }

    @Test
    void testEmptyInstance() {
        // Act
        RequestContext context = RequestContext.empty();

        // Assert
        assertThat(context).isNotNull();
        assertThat(context.requestAwareListeners()).isEmpty();
        assertThat(context.messageFilters()).isEmpty();
        assertThat(context.messagingErrorHandler()).isEqualTo(MessagingErrorHandler.DEFAULT);
        assertThat(context.headersAccessor()).isNotNull();
    }

    @Test
    void testConstructor() {
        // Arrange
        List<RequestAware> requestAwares = new ArrayList<>();
        requestAwares.add(new TestRequestAware());

        List<MessageFilter> filters = new ArrayList<>();
        filters.add(message -> false);

        MessagingErrorHandler errorHandler = MessagingErrorHandler.RETHROW;
        MessageHeadersAccessor headersAccessor = new MessageHeadersAccessor();
        MessageCompression compression = MessageCompression.newDefault();

        // Act
        RequestContext context = new RequestContext(requestAwares, filters, errorHandler, headersAccessor, compression);

        // Assert
        assertThat(context.requestAwareListeners()).isEqualTo(requestAwares);
        assertThat(context.messageFilters()).isEqualTo(filters);
        assertThat(context.messagingErrorHandler()).isEqualTo(errorHandler);
        assertThat(context.headersAccessor()).isEqualTo(headersAccessor);
    }

    @Test
    void testHandleRequestSuccessful() {
        // Arrange
        TestRequestAware requestAware = new TestRequestAware();
        List<RequestAware> requestAwares = List.of(requestAware);

        RequestContext context = new RequestContext(
                requestAwares,
                List.of(),
                MessagingErrorHandler.DEFAULT,
                new MessageHeadersAccessor(),
                MessageCompression.newDefault()
        );

        Message<MessageDetails> message = Message.just("test".getBytes(StandardCharsets.UTF_8));
        AtomicBoolean supplierCalled = new AtomicBoolean(false);

        // Act
        MessagingResult result = context.handleRequest(message, () -> {
            supplierCalled.set(true);
            return "result";
        });

        // Assert
        assertThat(result).isInstanceOf(MessagingResult.Success.class);
        assertThat(result.<String>getResultAs()).isEqualTo("result");
        assertThat(supplierCalled).isTrue();

        assertThat(requestAware.getStartedCalled()).isTrue();
        assertThat(requestAware.getSuccessfulCalled()).isTrue();
        assertThat(requestAware.getFailedCalled()).isFalse();
        assertThat(requestAware.getCompletedCalled()).isTrue();
    }

    @Test
    void testHandleRequestWithSkipFilter() {
        // Arrange
        TestRequestAware requestAware = new TestRequestAware();
        List<RequestAware> requestAwares = List.of(requestAware);

        MessageFilter skipFilter = message -> true; // Always skip
        List<MessageFilter> filters = List.of(skipFilter);

        RequestContext context = new RequestContext(
                requestAwares,
                filters,
                MessagingErrorHandler.DEFAULT,
                new MessageHeadersAccessor(),
                MessageCompression.newDefault()
        );

        Message<MessageDetails> message = Message.just("test".getBytes(StandardCharsets.UTF_8));
        AtomicBoolean supplierCalled = new AtomicBoolean(false);

        // Act
        MessagingResult result = context.handleRequest(message, () -> {
            supplierCalled.set(true);
            return "result";
        });

        // Assert
        assertThat(result).isInstanceOf(MessagingResult.SkipMessage.class);
        assertThat(supplierCalled).isFalse(); // Supplier should not be called when message is skipped

        assertThat(requestAware.getStartedCalled()).isTrue();
        assertThat(requestAware.getSuccessfulCalled()).isFalse();
        assertThat(requestAware.getFailedCalled()).isFalse();
        assertThat(requestAware.getCompletedCalled()).isTrue();
    }

    @Test
    void testHandleRequestWithException() {
        // Arrange
        TestRequestAware requestAware = new TestRequestAware();
        List<RequestAware> requestAwares = List.of(requestAware);

        RequestContext context = new RequestContext(
                requestAwares,
                List.of(),
                MessagingErrorHandler.DEFAULT,
                new MessageHeadersAccessor(),
                MessageCompression.newDefault()
        );

        Message<MessageDetails> message = Message.just("test".getBytes(StandardCharsets.UTF_8));
        RuntimeException exception = new RuntimeException("Test exception");

        // Act
        MessagingResult result = context.handleRequest(message, () -> {
            throw exception;
        });

        // Assert
        assertThat(result).isInstanceOf(MessagingResult.Failed.class);
        MessagingResult.Failed failedResult = (MessagingResult.Failed) result;
        assertThat(failedResult.error()).isSameAs(exception);

        assertThat(requestAware.getStartedCalled()).isTrue();
        assertThat(requestAware.getSuccessfulCalled()).isFalse();
        assertThat(requestAware.getFailedCalled()).isTrue();
        assertThat(requestAware.getFailedException()).isSameAs(exception);
        assertThat(requestAware.getCompletedCalled()).isTrue();
    }

    @Test
    void testHandleRequestWithRethrowErrorHandler() {
        // Arrange
        TestRequestAware requestAware = new TestRequestAware();
        List<RequestAware> requestAwares = List.of(requestAware);

        RequestContext context = new RequestContext(
                requestAwares,
                List.of(),
                MessagingErrorHandler.RETHROW,
                new MessageHeadersAccessor(),
                MessageCompression.newDefault()
        );

        Message<MessageDetails> message = Message.just("test".getBytes(StandardCharsets.UTF_8));
        RuntimeException exception = new RuntimeException("Test exception");

        // Act & Assert
        assertThatThrownBy(() -> context.handleRequest(message, () -> {
            throw exception;
        })).isSameAs(exception);

        assertThat(requestAware.getStartedCalled()).isTrue();
        assertThat(requestAware.getSuccessfulCalled()).isFalse();
        assertThat(requestAware.getFailedCalled()).isTrue();
        assertThat(requestAware.getFailedException()).isSameAs(exception);
        assertThat(requestAware.getCompletedCalled()).isTrue();
    }

    @Test
    void testHandleRequestWithExceptionInRequestAware() {
        // Arrange
        RuntimeException startedException = new RuntimeException("Started exception");
        RuntimeException failedException = new RuntimeException("Failed exception");
        RuntimeException completedException = new RuntimeException("Completed exception");

        // Create a RequestAware that throws exceptions, but doesn't throw in completed
        ExceptionThrowingRequestAware requestAware = new ExceptionThrowingRequestAware(
                startedException, failedException, null // No exception in completed
        );
        List<RequestAware> requestAwares = List.of(requestAware);

        AtomicReference<Throwable> caughtException = new AtomicReference<>();
        MessagingErrorHandler errorHandler = (message, throwable) -> {
            caughtException.set(throwable);
            return new MessagingResult.Failed(throwable);
        };

        RequestContext context = new RequestContext(
                requestAwares,
                List.of(),
                errorHandler,
                new MessageHeadersAccessor(),
                MessageCompression.newDefault()
        );

        Message<MessageDetails> message = Message.just("test".getBytes(StandardCharsets.UTF_8));
        RuntimeException supplierException = new RuntimeException("Supplier exception");

        // Act
        MessagingResult result = context.handleRequest(message, () -> {
            throw supplierException;
        });

        // Assert
        assertThat(result).isInstanceOf(MessagingResult.Failed.class);

        // The primary exception should be the one from started
        assertThat(caughtException.get()).isSameAs(startedException);

        // The failed exception should be suppressed
        assertThat(caughtException.get().getSuppressed()).hasSize(1);
        assertThat(caughtException.get().getSuppressed()[0]).isSameAs(failedException);
    }

    @Test
    void testHandleRequestWithMultipleRequestAwares() {
        // Arrange
        AtomicInteger startedOrder = new AtomicInteger(0);
        AtomicInteger successfulOrder = new AtomicInteger(0);
        AtomicInteger completedOrder = new AtomicInteger(0);

        OrderedTestRequestAware highPriority = new OrderedTestRequestAware(10, startedOrder, successfulOrder, completedOrder);
        OrderedTestRequestAware mediumPriority = new OrderedTestRequestAware(50, startedOrder, successfulOrder, completedOrder);
        OrderedTestRequestAware lowPriority = new OrderedTestRequestAware(100, startedOrder, successfulOrder, completedOrder);

        List<RequestAware> requestAwares = new ArrayList<>();
        requestAwares.add(lowPriority);
        requestAwares.add(highPriority);
        requestAwares.add(mediumPriority);
        requestAwares.sort(null); // Sort by order

        RequestContext context = new RequestContext(
                requestAwares,
                List.of(),
                MessagingErrorHandler.DEFAULT,
                new MessageHeadersAccessor(),
                MessageCompression.newDefault()
        );

        Message<MessageDetails> message = Message.just("test".getBytes(StandardCharsets.UTF_8));

        // Act
        MessagingResult result = context.handleRequest(message, () -> "result");

        // Assert
        assertThat(result).isInstanceOf(MessagingResult.Success.class);

        // Check that the RequestAware methods were called in the correct order
        assertThat(highPriority.getStartedCallOrder()).isEqualTo(1);
        assertThat(mediumPriority.getStartedCallOrder()).isEqualTo(2);
        assertThat(lowPriority.getStartedCallOrder()).isEqualTo(3);

        assertThat(highPriority.getSuccessfulCallOrder()).isEqualTo(1);
        assertThat(mediumPriority.getSuccessfulCallOrder()).isEqualTo(2);
        assertThat(lowPriority.getSuccessfulCallOrder()).isEqualTo(3);

        assertThat(highPriority.getCompletedCallOrder()).isEqualTo(1);
        assertThat(mediumPriority.getCompletedCallOrder()).isEqualTo(2);
        assertThat(lowPriority.getCompletedCallOrder()).isEqualTo(3);
    }

    // Test implementations

    private static class TestRequestAware implements RequestAware {
        private boolean startedCalled = false;
        private boolean successfulCalled = false;
        private boolean failedCalled = false;
        private boolean completedCalled = false;
        private Throwable failedException = null;

        @Override
        public Message<?> started(Message<?> message) {
            startedCalled = true;
            return message;
        }

        @Override
        public void successful(Message<?> message) {
            successfulCalled = true;
        }

        @Override
        public void failed(Message<?> message, Throwable throwable) {
            failedCalled = true;
            failedException = throwable;
        }

        @Override
        public void completed(Message<?> message) {
            completedCalled = true;
        }

        @Override
        public int getOrder() {
            return 0;
        }

        public boolean getStartedCalled() {
            return startedCalled;
        }

        public boolean getSuccessfulCalled() {
            return successfulCalled;
        }

        public boolean getFailedCalled() {
            return failedCalled;
        }

        public boolean getCompletedCalled() {
            return completedCalled;
        }

        public Throwable getFailedException() {
            return failedException;
        }
    }

    private static class ExceptionThrowingRequestAware implements RequestAware {
        private final RuntimeException startedException;
        private final RuntimeException failedException;
        private final RuntimeException completedException;

        public ExceptionThrowingRequestAware(
                RuntimeException startedException,
                RuntimeException failedException,
                RuntimeException completedException
        ) {
            this.startedException = startedException;
            this.failedException = failedException;
            this.completedException = completedException;
        }

        @Override
        public Message<?> started(Message<?> message) {
            throw startedException;
        }

        @Override
        public void successful(Message<?> message) {
            // This should not be called in the test
        }

        @Override
        public void failed(Message<?> message, Throwable throwable) {
            throw failedException;
        }

        @Override
        public void completed(Message<?> message) {
            if (completedException != null) {
                throw completedException;
            }
        }

        @Override
        public int getOrder() {
            return 0;
        }
    }

    private static class OrderedTestRequestAware implements RequestAware {
        private final int order;
        private final AtomicInteger startedOrder;
        private final AtomicInteger successfulOrder;
        private final AtomicInteger completedOrder;
        private int startedCallOrder = 0;
        private int successfulCallOrder = 0;
        private int completedCallOrder = 0;

        public OrderedTestRequestAware(
                int order,
                AtomicInteger startedOrder,
                AtomicInteger successfulOrder,
                AtomicInteger completedOrder
        ) {
            this.order = order;
            this.startedOrder = startedOrder;
            this.successfulOrder = successfulOrder;
            this.completedOrder = completedOrder;
        }

        @Override
        public Message<?> started(Message<?> message) {
            startedCallOrder = startedOrder.incrementAndGet();
            return message;
        }

        @Override
        public void successful(Message<?> message) {
            successfulCallOrder = successfulOrder.incrementAndGet();
        }

        @Override
        public void completed(Message<?> message) {
            completedCallOrder = completedOrder.incrementAndGet();
        }

        @Override
        public int getOrder() {
            return order;
        }

        public int getStartedCallOrder() {
            return startedCallOrder;
        }

        public int getSuccessfulCallOrder() {
            return successfulCallOrder;
        }

        public int getCompletedCallOrder() {
            return completedCallOrder;
        }
    }
}
