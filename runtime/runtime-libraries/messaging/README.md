# WireDI Messaging Library

The messaging library provides a small, self‑contained abstraction for transporting data as messages. A message is a combination of a payload, a set of headers, and optional transport‑specific details. The library is independent of any particular broker or transport and can be used standalone in plain Java. Integrations for popular transports build on top of the types described here and are available in the messaging integration module.

If you are looking for wiring and transport integrations, see the WireDI messaging integration module: ../../../integrations/messaging/README.md

## What this module includes

At its core, this module defines the types you use to represent and manipulate messages. Message<D extends MessageDetails> provides an immutable view of a payload together with headers and optional details and supports both byte[] and InputStream bodies. MessageHeaders and MessageHeader implement a compact, binary‑safe header model with fluent builders and typed encode/decode helpers. MessageDetails is a marker for transport‑specific context and includes a NONE singleton to avoid nulls. SimpleMessage and InputStreamMessage are the concrete message implementations for in‑memory and streaming payloads. MessagingContext is a lightweight registry for MessageConverter instances and MessageInterceptor hooks and offers cache‑aware conversion. MessageConverter is the SPI for serialization and deserialization between domain objects and Message; the library ships with basic converters for byte[] and String. MessageInterceptor lets you inspect or alter outbound messages after serialization. MessageHeadersAccessor provides a thread‑local slot to carry headers across boundaries. MessageFilter is a small predicate contract an engine can use to skip processing a message.

The following sections show how to work with these types in plain Java. Examples focus on constructing, transforming, and converting messages; they do not cover wiring or transport configuration. For integration and wiring, follow the link above to the messaging integration module.

## Creating messages and working with payloads

You can create messages from byte arrays or from input streams. The Message API offers a builder for each case.

```java
import com.wiredi.runtime.messaging.*;
import com.wiredi.runtime.messaging.messages.SimpleMessage;
import java.nio.charset.StandardCharsets;

// Create a simple message from bytes
byte[] body = "Hello, world!".getBytes(StandardCharsets.UTF_8);
Message<MessageDetails> m1 = Message.just(body);

// Or use the builder to add headers and details
Message<MessageDetails> m2 = Message
        .builder(body)
        .addHeader("content-type", "text/plain; charset=UTF-8")
        .withDetails(MessageDetails.NONE) // Replace with transport-specific details in an integration
        .build();

// Access the payload as bytes
byte[] bytes = m2.body();
long size = m2.bodySize();
```

For streaming data that should not be materialized immediately, wrap an InputStream. You can buffer later when needed.

```java
import com.wiredi.runtime.messaging.*;
import com.wiredi.runtime.messaging.messages.InputStreamMessage;
import java.io.*;

try (InputStream in = new FileInputStream("/path/to/file.bin")) {
    Message<MessageDetails> streaming = Message
            .builder(in)
            .addHeader("content-type", "application/octet-stream")
            .withDetails(MessageDetails.NONE)
            .build();

    // Stream directly to an OutputStream without loading everything in memory
    try (OutputStream out = new FileOutputStream("/tmp/copy.bin")) {
        streaming.writeBodyTo(out);
    }

    // If you really need a materialized copy, buffer returns a SimpleMessage with a byte[] body
    Message<MessageDetails> buffered = streaming.buffer();
    byte[] materialized = buffered.body();
}
```

Messages also support an explicit chunked flag that some transports use for framing. When you construct a message via InputStreamMessage.Builder, the message is considered chunked; SimpleMessage uses a byte[] body and is non‑chunked. You can inspect and modify this flag via isChunked() and setChunked(boolean) on the Message interface.

## Adding and reading headers

Headers are binary‑safe name/value pairs. You can add them through a fluent builder, or create single header entries with MessageHeader.of helpers that encode common Java types efficiently.

```java
import com.wiredi.runtime.messaging.*;
import java.time.Instant;
import java.nio.charset.StandardCharsets;

MessageHeaders headers = MessageHeaders.builder()
        .add("content-type", "application/json")
        .add(MessageHeader.of("x-retry-count", 0))
        .add(MessageHeader.of("x-created-at", Instant.now()))
        .build();

Message<MessageDetails> msg = Message
        .builder("{\"ok\":true}".getBytes(StandardCharsets.UTF_8))
        .addHeaders(headers)
        .build();

// Read headers
String contentType = msg.header("content-type").decodeToString();
int retries = msg.header("x-retry-count").decodeToInt();
Instant createdAt = msg.header("x-created-at").decodeToInstant();

// Work with multiple header values
MessageHeader first = msg.getFirstHeader("x-custom");
MessageHeader last = msg.getLastHeader("x-custom");
```

MessageHeader provides decodeToShort, decodeToInt, decodeToLong, decodeToFloat, decodeToDouble, decodeToInstant, and decodeToEnum helpers. This avoids ad‑hoc parsing and keeps headers efficient.

## Carrying headers implicitly with MessageHeadersAccessor

When you cross component boundaries, you may not want to pass headers through every method signature. MessageHeadersAccessor offers a thread‑local slot for the “current” headers so that integrations can pick them up implicitly.

```java
import com.wiredi.runtime.messaging.*;

MessageHeadersAccessor accessor = new MessageHeadersAccessor();
MessageHeaders current = MessageHeaders.builder().add("trace-id", "abc-123").build();

accessor.doWith(current, () -> {
    // Inside this runnable, accessor.getCurrentHeaders() returns current
    MessageHeaders seen = accessor.getCurrentHeaders();
    System.out.println("Trace: " + seen.firstValue("trace-id").decodeToString());
});

// Outside, the previous value is restored (or cleared)
```

Integrations that support the accessor can populate it for inbound messages and consult it when sending outbound messages, enabling simple context propagation without polluting your APIs.

## Converting between domain objects and Message

Serialization and deserialization are handled by MessageConverter implementations. The library includes ByteArrayMessageConverter and StringMessageConverter for common cases and provides MessagingContext to orchestrate conversion with simple caching of the last successful converter per (target type, details) pair.

You can use MessagingContext.defaultContext() for out‑of‑the‑box conversion between String and byte[] messages. You can also register your own converters on a dedicated context instance.

```java
import com.wiredi.runtime.messaging.*;

MessagingContext ctx = MessagingContext.defaultContext();

// Deserialize a String payload from a message using the context
Message<MessageDetails> msg = Message.just("Hello".getBytes());
String text = ctx.convertCacheAware(String.class, msg.details(), converter -> {
    if (!converter.canDeserialize(msg, String.class)) return null; // try next converter
    @SuppressWarnings("unchecked")
    String value = ((MessageConverter<String, MessageDetails>) converter)
            .deserialize((Message<MessageDetails>) msg, String.class);
    return value; // non-null stops searching and is cached for future calls
});

// Serialize a domain object by asking converters to produce a Message
record User(String username) {}

// Suppose we have a converter for User -> Message registered (see below)
MessageHeaders headers = MessageHeaders.builder().add("content-type", "application/json").build();
User user = new User("jane.doe");
Message<MessageDetails> out = ctx.convertCacheAware(Message.class, MessageDetails.NONE, converter -> {
    if (!converter.canSerialize(user, headers, MessageDetails.NONE)) return null; // try next
    @SuppressWarnings("unchecked")
    Message<MessageDetails> m = ((MessageConverter<Object, MessageDetails>) converter)
            .serialize(user, headers, MessageDetails.NONE);
    return m;
});
```

To provide custom conversion logic, implement MessageConverter. You can then register it on a dedicated MessagingContext via context.converters().add(new MyConverter()).

```java
import com.wiredi.runtime.messaging.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.nio.charset.StandardCharsets;

// Example: a minimal JSON-ish converter for the User record
class UserJsonMessageConverter implements MessageConverter<User, MessageDetails> {
    @Override
    public boolean canSerialize(@NotNull Object payload, @NotNull MessageHeaders headers, @NotNull MessageDetails details) {
        return payload instanceof User;
    }

    @Override
    public @Nullable Message<MessageDetails> serialize(@NotNull Object payload, @NotNull MessageHeaders headers, @NotNull MessageDetails details) {
        if (!(payload instanceof User u)) return null;
        String json = "{\"username\":\"" + u.username() + "\"}";
        return Message.builder(json.getBytes(StandardCharsets.UTF_8))
                .addHeaders(headers)
                .withDetails(details)
                .build();
    }

    @Override
    public boolean canDeserialize(@NotNull Message<?> message, @NotNull Class<?> targetType) {
        MessageHeader h = message.header("content-type");
        return targetType == User.class && h != null && h.decodeToString().contains("json");
    }

    @Override
    public @Nullable User deserialize(@NotNull Message<MessageDetails> message, @NotNull Class<User> targetType) {
        String json = message.header("content-type") != null ? new String(message.body(), StandardCharsets.UTF_8) : null;
        if (json == null) return null;
        // naive extraction for illustration only
        String username = json.replaceAll(".*\\\"username\\\":\\\"(.*?)\\\".*", "$1");
        return new User(username);
    }
}

// Register your converter on a fresh context
MessagingContext custom = MessagingContext.empty();
custom.converters().add(new UserJsonMessageConverter());
```

## Intercepting outbound messages

MessageInterceptor lets you inspect and alter a message right after serialization and before it is sent by an integration. You can use this to add tracing headers, sign messages, or implement auditing.

```java
import com.wiredi.runtime.messaging.*;

class TracingInterceptor implements MessageInterceptor {
    @Override
    public <D extends MessageDetails> Message<D> postConstruction(Message<D> message) {
        return message.copyWithPayload(message.body()) // keep body as-is
                .mapHeaders(builder -> builder.add("trace-id", "abc-123"));
    }
}

MessagingContext ctx = MessagingContext.empty();
ctx.messageInterceptors().add(new TracingInterceptor());

// When you serialize, apply interceptors before handing the message to your transport
Message<MessageDetails> serialized = Message.just("payload".getBytes());
for (MessageInterceptor interceptor : ctx.messageInterceptors()) {
    serialized = interceptor.postConstruction(serialized);
}
```

Integrations typically take care of running interceptors at the appropriate time. The example demonstrates the simple rule: treat messages as immutable and replace the instance when you alter it.

## Filters and details

MessageFilter is a small predicate interface that an engine can use to skip specific messages. You implement it to express rules, such as rejecting messages without required headers or those exceeding a size limit. MessageDetails lets transports provide context objects for inbound and outbound messages without polluting your application code with transport‑specific types. If you do not have details to attach, use MessageDetails.NONE.

## Where to go next

This library intentionally stops short of transport concerns. For wiring, channels, subscriptions and engine implementations that integrate with brokers or frameworks, refer to the messaging integration module in this repository:

- ../../../integrations/messaging/README.md

That module shows how to plug these core abstractions into a running application while keeping your code portable and testable.
