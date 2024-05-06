package com.wiredi.runtime.qualifier;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.BiConsumer;

public record QualifierType(String name, Map<String, List<String>> values) {

    public QualifierType(String name, Map<String, List<String>> values) {
        this.name = name;
        this.values = Collections.unmodifiableMap(values);
    }

    public static QualifierType just(String name) {
        return newInstance(name).build();
    }

    public static QualifierType just(Class<? extends Annotation> type) {
        return newInstance(type).build();
    }

    public static Builder newInstance(String name) {
        return new Builder(name);
    }

    public static Builder newInstance(Class<? extends Annotation> type) {
        return newInstance(type.getName());
    }

    public void forEach(BiConsumer<String, Object> consumer) {
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
    public String toString() {
        if (values.isEmpty()) {
            return "Qualifier(" + name + ")";
        } else {
            return "Qualifier(" + name + "=>" + values + ")";
        }
    }

    public static class Builder {

        private final String name;
        private final Map<String, List<String>> values = new HashMap<>();

        public Builder(String name) {
            this.name = name;
        }

        public Builder add(String name, Object... values) {
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

        public Builder add(String name, Enum<?> value) {
            return add(name, value.name());
        }

        public Builder add(String name, Class<?> value) {
            return add(name, value.getName());
        }

        public Builder add(String name, String value) {
            this.values.computeIfAbsent(name, (k) -> new ArrayList<>()).add(value);
            return this;
        }

        public Builder addAll(Map<String, Object> values) {
            values.forEach(this::add);
            return this;
        }

        public QualifierType build() {
            return new QualifierType(name, values);
        }
    }
}
