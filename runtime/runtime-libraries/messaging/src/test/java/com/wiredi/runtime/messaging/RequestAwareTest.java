package com.wiredi.runtime.messaging;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RequestAwareTest {

    @Test
    void testDefaultImplementation() {
        // Arrange
        RequestAware requestAware = new DefaultRequestAware();
        Message<MessageDetails> message = Message.just("test".getBytes(StandardCharsets.UTF_8));
        RuntimeException exception = new RuntimeException("Test exception");

        // Act & Assert
        // Default implementation should return the same message
        assertThat(requestAware.started(message)).isSameAs(message);
        
        // Default implementations of other methods should not throw exceptions
        requestAware.successful(message);
        requestAware.failed(message, exception);
        requestAware.completed(message);
    }

    @Test
    void testCustomImplementation() {
        // Arrange
        TrackingRequestAware requestAware = new TrackingRequestAware();
        Message<MessageDetails> message = Message.just("test".getBytes(StandardCharsets.UTF_8));
        RuntimeException exception = new RuntimeException("Test exception");

        // Act
        Message<?> modifiedMessage = requestAware.started(message);
        requestAware.successful(message);
        requestAware.failed(message, exception);
        requestAware.completed(message);

        // Assert
        assertThat(modifiedMessage).isNotSameAs(message);
        assertThat(new String(modifiedMessage.body(), StandardCharsets.UTF_8)).isEqualTo("modified test");
        assertThat(requestAware.getStartedCalled()).isTrue();
        assertThat(requestAware.getSuccessfulCalled()).isTrue();
        assertThat(requestAware.getFailedCalled()).isTrue();
        assertThat(requestAware.getCompletedCalled()).isTrue();
        assertThat(requestAware.getFailedException()).isSameAs(exception);
    }

    @Test
    void testOrdering() {
        // Arrange
        RequestAware lowPriority = new OrderedRequestAware(100);
        RequestAware mediumPriority = new OrderedRequestAware(50);
        RequestAware highPriority = new OrderedRequestAware(10);

        // Act & Assert
        assertThat(lowPriority.getOrder()).isEqualTo(100);
        assertThat(mediumPriority.getOrder()).isEqualTo(50);
        assertThat(highPriority.getOrder()).isEqualTo(10);
        
        // Higher priority (lower order value) should come first
        List<RequestAware> requestAwares = new ArrayList<>();
        requestAwares.add(lowPriority);
        requestAwares.add(mediumPriority);
        requestAwares.add(highPriority);
        
        requestAwares.sort(null); // Sort using natural ordering (Ordered interface)
        
        assertThat(requestAwares).containsExactly(highPriority, mediumPriority, lowPriority);
    }

    @Test
    void testLifecycleOrder() {
        // Arrange
        LifecycleTrackingRequestAware requestAware = new LifecycleTrackingRequestAware();
        Message<MessageDetails> message = Message.just("test".getBytes(StandardCharsets.UTF_8));
        RuntimeException exception = new RuntimeException("Test exception");

        // Act - Successful path
        requestAware.reset();
        requestAware.started(message);
        requestAware.successful(message);
        requestAware.completed(message);

        // Assert - Successful path
        assertThat(requestAware.getEvents()).containsExactly("started", "successful", "completed");

        // Act - Failed path
        requestAware.reset();
        requestAware.started(message);
        requestAware.failed(message, exception);
        requestAware.completed(message);

        // Assert - Failed path
        assertThat(requestAware.getEvents()).containsExactly("started", "failed", "completed");
    }

    // Test implementations

    private static class DefaultRequestAware implements RequestAware {
        @Override
        public int getOrder() {
            return 0;
        }
    }

    private static class TrackingRequestAware implements RequestAware {
        private boolean startedCalled = false;
        private boolean successfulCalled = false;
        private boolean failedCalled = false;
        private boolean completedCalled = false;
        private Throwable failedException = null;

        @Override
        public Message<?> started(Message<?> message) {
            startedCalled = true;
            String originalContent = new String(message.body(), StandardCharsets.UTF_8);
            String modifiedContent = "modified " + originalContent;
            return Message.just(modifiedContent.getBytes(StandardCharsets.UTF_8), message.headers(), message.details());
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

    private static class OrderedRequestAware implements RequestAware {
        private final int order;

        public OrderedRequestAware(int order) {
            this.order = order;
        }

        @Override
        public int getOrder() {
            return order;
        }
    }

    private static class LifecycleTrackingRequestAware implements RequestAware {
        private final List<String> events = new ArrayList<>();

        @Override
        public Message<?> started(Message<?> message) {
            events.add("started");
            return message;
        }

        @Override
        public void successful(Message<?> message) {
            events.add("successful");
        }

        @Override
        public void failed(Message<?> message, Throwable throwable) {
            events.add("failed");
        }

        @Override
        public void completed(Message<?> message) {
            events.add("completed");
        }

        @Override
        public int getOrder() {
            return 0;
        }

        public List<String> getEvents() {
            return events;
        }

        public void reset() {
            events.clear();
        }
    }
}