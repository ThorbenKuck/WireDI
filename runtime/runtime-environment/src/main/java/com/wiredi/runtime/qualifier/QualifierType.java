package com.wiredi.runtime.qualifier;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.BiConsumer;

public record QualifierType(@NotNull String name, @NotNull Map<@NotNull String, @NotNull List<@NotNull String>> values) {

    @NotNull
    public static QualifierType just(@NotNull String name) {
        return builder(name).build();
    }

    @NotNull
    public static QualifierType just(@NotNull Class<? extends Annotation> type) {
        return builder(type).build();
    }

    @NotNull
    public static Builder builder(@NotNull String name) {
        return new Builder(name);
    }

    @NotNull
    public static Builder builder(@NotNull Class<? extends Annotation> type) {
        return builder(type.getName());
    }

    public void forEach(@NotNull BiConsumer<@NotNull String, @NotNull Object> consumer) {
        values.forEach((key, values) -> {
            values.forEach(value -> consumer.accept(key, value));
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QualifierType qualifierType = (QualifierType) o;
        return Objects.equals(name, qualifierType.name) && Objects.equals(values, qualifierType.values);
    }

    @Override
    public @NotNull String toString() {
        if (values.isEmpty()) {
            return "Qualifier(" + name + ")";
        } else {
            return "Qualifier(" + name + "=>" + values + ")";
        }
    }

    public static class Builder {

        @NotNull private final String name;
        @NotNull private final Map<@NotNull String, @NotNull List<@NotNull String>> values = new HashMap<>();

        public Builder(@NotNull String name) {
            this.name = name;
        }

        @NotNull
        public Builder add(@NotNull String name, @NotNull Object... values) {
            // Support annotations and arrays
            for (Object value : values) {
                if (value instanceof Class<?> c) {
                    add(name, c);
                } else if (value instanceof Enum<?> e) {
                    add(name, e);
                } else {
                    add(name, value.toString());
                }
            }

            return this;
        }

        @NotNull
        public Builder add(@NotNull String name, @NotNull Enum<?> value) {
            return add(name, value.name());
        }

        @NotNull
        public Builder add(@NotNull String name, @NotNull Class<?> value) {
            return add(name, value.getName());
        }

        @NotNull
        public Builder add(@NotNull String name, @NotNull String value) {
            this.values.computeIfAbsent(name, (k) -> new ArrayList<>()).add(value);
            return this;
        }

        @NotNull
        public Builder addAll(@NotNull Map<@NotNull String, @NotNull Object> values) {
            values.forEach(this::add);
            return this;
        }

        @NotNull
        public QualifierType build() {
            return new QualifierType(name, values);
        }
    }
}
