package com.wiredi.runtime.messaging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.BiConsumer;

/**
 * A generic representation of Headers.
 * <p>
 * Headers are a map of names, pointing to a list of values.
 * They are used to pass additional information with requests or responses in different technologies.
 * One example of this is the Http headers.
 * <p>
 * Whenever integrating a technology that supports headers, it is recommended to use this class here wiredi internally.
 * This way, the integration will be technology independent.
 * Integrations should (if possible) not use the technology-dependent header classes.
 * <p>
 * If required, technology-dependent details can be transported in the {@link MessageDetails}.
 * But please note that even here it is recommended to use abstraction layers wherever applicable.
 */
public class Headers implements Iterable<HeaderEntry> {

    @NotNull
    public static final Headers EMPTY = new Headers(Collections.emptyMap());

    @NotNull
    private final Map<@NotNull String, @NotNull List<@NotNull HeaderEntry>> values;

    private Headers(@NotNull Map<@NotNull String, @NotNull List<@NotNull HeaderEntry>> values) {
        this.values = Collections.unmodifiableMap(values);
    }

    public static Headers of(Map<String, ? extends Collection<String>> headers) {
        return builder().addAll(headers).build();
    }

    public static Headers of(Iterable<HeaderEntry> headers) {
        Builder builder = builder();
        headers.forEach(builder::add);
        return builder.build();
    }

    @NotNull
    public static Headers.Builder builder() {
        return new Builder();
    }

    @NotNull
    public static Headers.Builder builder(Headers headers) {
        return new Builder(headers);
    }

    public Headers copy() {
        return new Headers(values);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Nullable
    public HeaderEntry firstValue(@NotNull String name) {
        List<HeaderEntry> headers = allValues(name);
        if (headers.isEmpty()) {
            return null;
        }
        return headers.getFirst();
    }

    @Nullable
    public HeaderEntry lastValue(@NotNull String name) {
        List<HeaderEntry> headers = allValues(name);
        if (headers.isEmpty()) {
            return null;
        }

        return headers.getLast();
    }

    @NotNull
    public List<HeaderEntry> allValues(@NotNull String name) {
        List<HeaderEntry> values = this.values.get(name);
        return values != null ? values : Collections.emptyList();
    }

    @NotNull
    public Map<String, List<HeaderEntry>> map() {
        return this.values;
    }

    public void forEach(BiConsumer<String, List<HeaderEntry>> consumer) {
        this.values.forEach(consumer);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Headers that)) return false;
        return Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

    @Override
    @NotNull
    public String toString() {
        return "HttpHeaders{ " + map() + " }";
    }

    @NotNull
    @Override
    public Iterator<HeaderEntry> iterator() {
        return values.values()
                .stream()
                .flatMap(Collection::stream)
                .toList()
                .iterator();
    }

    public static class Builder {

        @NotNull
        private final Map<@NotNull String, @NotNull List<@NotNull HeaderEntry>> entries;

        public Builder() {
            entries = new HashMap<>();
        }

        public Builder(Headers headers) {
            entries = new HashMap<>(headers.values);
        }

        @NotNull
        public Builder add(
                @NotNull String name,
                byte[] value
        ) {
            return add(new HeaderEntry(name, value));
        }

        @NotNull
        public Builder add(
                @NotNull String name,
                @NotNull String value
        ) {
            return add(new HeaderEntry(name, value.getBytes(StandardCharsets.UTF_8)));
        }

        @NotNull
        public Builder add(
                @NotNull HeaderEntry header
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
        public <C extends Collection<HeaderEntry>> Builder addAll(
                @NotNull String name,
                @NotNull C values
        ) {
            entries.computeIfAbsent(name, n -> new ArrayList<>()).addAll(values);
            return this;
        }

        @NotNull
        public Builder addAll(
                @NotNull Headers headers
        ) {
            this.entries.putAll(headers.values);
            return this;
        }

        public boolean isEmpty() {
            return entries.isEmpty();
        }

        @Nullable
        public HeaderEntry firstValue(@NotNull String name) {
            List<HeaderEntry> headers = allValues(name);
            if (headers.isEmpty()) {
                return null;
            }
            return headers.getFirst();
        }

        @Nullable
        public HeaderEntry lastValue(@NotNull String name) {
            List<HeaderEntry> headers = allValues(name);
            if (headers.isEmpty()) {
                return null;
            }

            return headers.getLast();
        }

        @NotNull
        public List<HeaderEntry> allValues(@NotNull String name) {
            List<HeaderEntry> values = this.entries.get(name);
            return values != null ? values : Collections.emptyList();
        }

        @NotNull
        public Headers build() {
            if (entries.isEmpty()) {
                return EMPTY;
            }

            return new Headers(entries);
        }
    }
}
