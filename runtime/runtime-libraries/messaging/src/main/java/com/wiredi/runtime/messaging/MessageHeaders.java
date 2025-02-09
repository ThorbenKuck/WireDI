package com.wiredi.runtime.messaging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * A generic representation of Headers.
 * <p>
 * Headers are a map of names, pointing to a list of values.
 * They're used to pass additional information with requests or responses in different technologies.
 * One example of this is the Http headers.
 * <p>
 * Whenever integrating a technology that supports headers, it is recommended to use this class here wiredi internally.
 * This way, the integration is technology independent.
 * Integrations should (if possible) not use the technology-dependent header classes.
 * <p>
 * If required, technology-dependent details can be transported in the {@link MessageDetails}.
 * But please note that even here it is recommended to use abstraction layers wherever applicable.
 */
public class MessageHeaders implements Iterable<MessageHeader> {

    @NotNull
    private final Map<@NotNull String, @NotNull List<@NotNull MessageHeader>> values;

    public MessageHeaders() {
        this.values = new HashMap<>();
    }

    public MessageHeaders(@NotNull Map<@NotNull String, @NotNull List<@NotNull MessageHeader>> values) {
        this.values = new HashMap<>(values);
    }

    public static MessageHeaders of(Map<String, ? extends Collection<String>> headers) {
        return builder().addAll(headers).build();
    }

    public static MessageHeaders of(Iterable<MessageHeader> headers) {
        Builder builder = builder();
        headers.forEach(builder::add);
        return builder.build();
    }

    @NotNull
    public static MessageHeaders.Builder builder() {
        return new Builder();
    }

    @NotNull
    public static MessageHeaders.Builder builder(MessageHeaders headers) {
        return new Builder(headers);
    }

    public MessageHeaders.Builder copy() {
        return new Builder(this);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Nullable
    public MessageHeader firstValue(@NotNull String name) {
        List<MessageHeader> headers = allValues(name);
        if (headers.isEmpty()) {
            return null;
        }
        return headers.getFirst();
    }

    @Nullable
    public MessageHeader lastValue(@NotNull String name) {
        List<MessageHeader> headers = allValues(name);
        if (headers.isEmpty()) {
            return null;
        }

        return headers.getLast();
    }

    @NotNull
    public List<MessageHeader> allValues(@NotNull String name) {
        List<MessageHeader> values = this.values.get(name);
        return values != null ? values : Collections.emptyList();
    }

    @NotNull
    public Map<String, List<MessageHeader>> map() {
        return Collections.unmodifiableMap(this.values);
    }

    public void forEach(BiConsumer<String, List<MessageHeader>> consumer) {
        this.values.forEach(consumer);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof MessageHeaders that)) return false;
        return Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

    @Override
    @NotNull
    public String toString() {
        return "MessageHeaders{ " + this.values + " }";
    }

    @NotNull
    @Override
    public Iterator<MessageHeader> iterator() {
        return values.values()
                .stream()
                .flatMap(Collection::stream)
                .toList()
                .iterator();
    }

    public static class Builder {

        @NotNull
        private final Map<@NotNull String, @NotNull List<@NotNull MessageHeader>> entries;

        public Builder() {
            entries = new HashMap<>();
        }

        public Builder(MessageHeaders headers) {
            entries = new HashMap<>(headers.values);
        }

        public Builder set(
                @NotNull String name,
                byte[] value
        ) {
            return set(new MessageHeader(name, value));
        }

        public Builder set(
                @NotNull String name,
                String value
        ) {
            return set(MessageHeader.of(name, value));
        }

        public Builder set(MessageHeader messageHeader) {
            this.entries.put(messageHeader.name(), List.of(messageHeader));
            return this;
        }

        @NotNull
        public Builder add(
                @NotNull String name,
                byte[] value
        ) {
            return add(new MessageHeader(name, value));
        }

        @NotNull
        public Builder add(
                @NotNull String name,
                @NotNull String value
        ) {
            return add(new MessageHeader(name, value.getBytes(StandardCharsets.UTF_8)));
        }

        @NotNull
        public Builder add(
                @NotNull MessageHeader header
        ) {
            entries.computeIfAbsent(header.name(), n -> new ArrayList<>()).add(header);
            return this;
        }

        @NotNull
        public Builder addAll(Map<String, ? extends Collection<String>> headers) {
            headers.forEach((name, values) -> values.forEach(value -> add(name, value)));
            return this;
        }

        @NotNull
        public Builder clear() {
            entries.clear();
            return this;
        }

        @NotNull
        public <C extends Collection<MessageHeader>> Builder addAll(
                @NotNull String name,
                @NotNull C values
        ) {
            entries.computeIfAbsent(name, n -> new ArrayList<>()).addAll(values);
            return this;
        }

        @NotNull
        public Builder addAll(
                @NotNull Iterable<MessageHeader> headers
        ) {
            headers.forEach(this::add);
            return this;
        }

        public boolean isEmpty() {
            return entries.isEmpty();
        }

        @Nullable
        public MessageHeader firstValue(@NotNull String name) {
            List<MessageHeader> headers = allValues(name);
            if (headers.isEmpty()) {
                return null;
            }
            return headers.getFirst();
        }

        @Nullable
        public MessageHeader lastValue(@NotNull String name) {
            List<MessageHeader> headers = allValues(name);
            if (headers.isEmpty()) {
                return null;
            }

            return headers.getLast();
        }

        @NotNull
        public List<MessageHeader> allValues(@NotNull String name) {
            List<MessageHeader> values = this.entries.get(name);
            return values != null ? values : Collections.emptyList();
        }

        @NotNull
        public MessageHeaders build() {
            return new MessageHeaders(entries);
        }

        public Map<@NotNull String, @NotNull List<@NotNull MessageHeader>> snapshot() {
            return Collections.unmodifiableMap(entries);
        }
    }
}
