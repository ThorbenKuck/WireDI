package com.wiredi.tracer;

import com.wiredi.metrics.MicrometerTraceToken;
import com.wiredi.runtime.messaging.MessageHeaders;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;

import java.util.function.Consumer;

public class MessageTracer {

    private final Tracer tracer;

    public MessageTracer(Tracer tracer) {
        this.tracer = tracer;
    }

    public Span createSpan(MessageHeaders messageHeaders) {
        return createSpan(messageHeaders, span -> {});
    }

    public Span createSpan(MessageHeaders messageHeaders, Consumer<Span> spanCustomizer) {
        MicrometerTraceToken token = MicrometerTraceToken.of(messageHeaders);
        if (token == null) {
            Span span = tracer.nextSpan();
            spanCustomizer.accept(span);
            return span.start();
        } else {
            TraceContext traceContext = token.createTraceContext(tracer);
            return tracer.spanBuilder()
                    .setParent(traceContext)
                    .start();
        }
    }

    public void runInSpan(MessageHeaders messageHeaders, Runnable runnable) {
        runInSpan(messageHeaders, span -> {}, runnable);
    }

    public void runInSpan(MessageHeaders messageHeaders, Consumer<Span> spanCustomizer, Runnable runnable) {
        Span span = createSpan(messageHeaders, spanCustomizer);
        try {
            runnable.run();
        } finally {
            span.end();
        }
    }

    public void injectSpan(MessageHeaders.Builder headers) {
        Span span = tracer.currentSpan();
        if (span == null || span.isNoop()) {
            return;
        }

        MicrometerTraceToken token = MicrometerTraceToken.of(span.context());
        token.injectInto(headers);
    }
}
