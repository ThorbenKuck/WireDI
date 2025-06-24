package com.wiredi.metrics;

import com.wiredi.runtime.messaging.MessageHeader;
import com.wiredi.runtime.messaging.MessageHeaders;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.checkerframework.checker.units.qual.N;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record MicrometerTraceToken(
        @NotNull String traceId,
        @NotNull String spanId,
        @Nullable String parentId,
        @Nullable Boolean sampled
) {
    @Nullable
    public static MicrometerTraceToken of(MessageHeaders headers) {
        MessageHeader messageHeader = headers.firstValue("trace-token");
        if (messageHeader == null) {
            return null;
        }

        String headerValue = messageHeader.decodeToString();
        if (headerValue.isBlank() || !headerValue.startsWith("micrometer:")) {
            return null;
        }

        String[] split = headerValue.replaceFirst("micrometer:", "").split("&");

        if (split.length != 4) {
            return null;
        }

        String traceId = split[0];
        String spanId = split[1];
        String rawParentId = split[2];
        String rawSampled = split[3];
        String parentId = rawParentId.isBlank() ? null : rawParentId;
        Boolean sampled = rawSampled.isBlank() ? null : Boolean.parseBoolean(rawSampled);

        return new MicrometerTraceToken(traceId, spanId, parentId, sampled);
    }

    public static MicrometerTraceToken of(TraceContext context) {
        return new MicrometerTraceToken(context.traceId(), context.spanId(), context.parentId(), context.sampled());
    }

    public TraceContext createTraceContext(Tracer tracer) {
        TraceContext.Builder builder = tracer.traceContextBuilder()
                .traceId(traceId)
                .spanId(spanId);

        if (parentId != null) builder.parentId(parentId);
        if (sampled != null) builder.sampled(sampled);

        return builder.build();
    }

    public void injectInto(MessageHeaders.Builder headers) {
        headers.set("trace-token", "micrometer:" +
                traceId + "&" +
                spanId + "&" +
                (parentId == null ? "" : parentId) + "&" +
                (sampled == null ? "" : sampled));
    }
}
