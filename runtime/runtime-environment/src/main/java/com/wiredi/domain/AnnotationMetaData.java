package com.wiredi.domain;

import com.google.common.primitives.Primitives;
import com.wiredi.domain.provider.TypeIdentifier;
import com.wiredi.lang.collections.EnumSet;

import javax.lang.model.element.AnnotationMirror;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AnnotationMetaData {

    private final Map<String, Object> fields;
    private final String className;

    public AnnotationMetaData(String className, Map<String, Object> fields) {
        this.fields = fields;
        this.className = className;
    }

    // ########### Builder ###########

    public static Builder newInstance(String className) {
        return new Builder(className);
    }

    public static AnnotationMetaData empty(String className) {
        return new AnnotationMetaData(className, Collections.emptyMap());
    }

    public static AnnotationMetaData of(AnnotationMirror mirror) {
        Builder builder = newInstance(mirror.getAnnotationType().asElement().toString());
        mirror.getElementValues().forEach((method, annotationValue) -> {
            Object fieldValue = annotationValue.getValue();
            final String field = method.getSimpleName().toString();
            if (AnnotationMirror.class.isAssignableFrom(fieldValue.getClass())) {
                builder.fields.put(field, AnnotationMetaData.of((AnnotationMirror) fieldValue));
            } else {
                builder.fields.put(field, fieldValue);
            }
        });
        return builder.build();
    }

    public static <T extends Annotation> AnnotationMetaData of(T annotation) {
        Class<? extends Annotation> annotationType = annotation.getClass();
        Builder builder = newInstance(annotationType.getName());

        Arrays.stream(annotationType.getDeclaredFields()).forEach(field -> {
            Object fieldValue;
            try {
                field.trySetAccessible();
                fieldValue = field.get(annotation);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
            if (field.getType().isAnnotation()) {
                builder.fields.put(field.getName(), AnnotationMetaData.of((AnnotationMirror) fieldValue));
            } else {
                builder.fields.put(field.getName(), fieldValue);
            }
        });

        return builder.build();
    }

    public Optional<String> get(String field) {
        Object value = fields.get(field);

        if (value instanceof String) {
            return Optional.ofNullable((String) value);
        }

        return Optional.empty();
    }

    // ########### Accessors ###########

    public Optional<Boolean> getBoolean(String field) {
        Object value = fields.get(field);

        if (value instanceof Boolean) {
            return Optional.ofNullable((Boolean) value);
        } else if (value instanceof String) {
            return Optional.ofNullable((String) value).map(Boolean::valueOf);
        }

        return Optional.empty();
    }

    public Optional<Integer> getInt(String field) {
        Object value = fields.get(field);

        if (value instanceof Integer) {
            return Optional.ofNullable((Integer) value);
        } else if (value instanceof String) {
            return Optional.ofNullable((String) value).map(Integer::valueOf);
        }

        return Optional.empty();
    }

    public Optional<Long> getLong(String field) {
        Object value = fields.get(field);

        if (value instanceof Long) {
            return Optional.ofNullable((Long) value);
        } else if (value instanceof String) {
            return Optional.ofNullable((String) value).map(Long::valueOf);
        }

        return Optional.empty();
    }

    public Optional<Float> getFloat(String field) {
        Object value = fields.get(field);

        if (value instanceof Float) {
            return Optional.ofNullable((Float) value);
        } else if (value instanceof String) {
            return Optional.ofNullable((String) value).map(Float::valueOf);
        }

        return Optional.empty();
    }

    public Optional<Double> getDouble(String field) {
        Object value = fields.get(field);

        if (value instanceof Double) {
            return Optional.ofNullable((Double) value);
        } else if (value instanceof String) {
            return Optional.ofNullable((String) value).map(Double::valueOf);
        }

        return Optional.empty();
    }

    public Optional<Class<?>> getClass(String field) {
        Object value = fields.get(field);

        if (value instanceof Class<?>) {
            return Optional.ofNullable((Class<?>) value);
        } else if (value instanceof String) {
            return Optional.ofNullable((String) value).map(rawValue -> {
                try {
                    return Class.forName(rawValue);
                } catch (ClassNotFoundException e) {
                    return null;
                }
            });
        }

        return Optional.empty();
    }

    public void forEach(BiConsumer<String, Object> consumer) {
        fields.forEach(consumer);
    }

    public Optional<TypeIdentifier<?>> getType(String field) {
        return getClass(field).map(TypeIdentifier::just);
    }

    public <T extends Enum<T>> Optional<T> getEnum(String field, Class<T> enumType) {
        Object value = fields.get(field);

        if (enumType.isAssignableFrom(value.getClass())) {
            return Optional.ofNullable((T) value);
        } else if (value instanceof String) {
            return Optional.ofNullable((String) value).map(fieldValue -> EnumSet.of(enumType).require(fieldValue));
        }

        return Optional.empty();
    }

    public Optional<AnnotationMetaData> getAnnotation(String field) {
        Object value = fields.get(field);

        if (value instanceof AnnotationMetaData) {
            return Optional.ofNullable((AnnotationMetaData) value);
        }

        return Optional.empty();
    }

    public String require(String field) {
        return get(field).orElseThrow(() -> new IllegalStateException("The annotation did not contain a field named " + field));
    }

    public Boolean requireBoolean(String field) {
        return getBoolean(field).orElseThrow(() -> new IllegalStateException("The annotation did not contain a field named " + field));
    }

    public Integer requireInt(String field) {
        return getInt(field).orElseThrow(() -> new IllegalStateException("The annotation did not contain a field named " + field));
    }

    public Long requireLong(String field) {
        return getLong(field).orElseThrow(() -> new IllegalStateException("The annotation did not contain a field named " + field));
    }

    public Float requireFloat(String field) {
        return getFloat(field).orElseThrow(() -> new IllegalStateException("The annotation did not contain a field named " + field));
    }

    public Double requireDouble(String field) {
        return getDouble(field).orElseThrow(() -> new IllegalStateException("The annotation did not contain a field named " + field));
    }

    public Class<?> requireClass(String field) {
        return getClass(field).orElseThrow(() -> new IllegalStateException("The annotation did not contain a field named " + field));
    }

    public TypeIdentifier<?> requireType(String field) {
        return TypeIdentifier.just(requireClass(field));
    }

    public <T extends Enum<T>> T requireEnum(String field, Class<T> enumType) {
        return getEnum(field, enumType).orElseThrow(() -> new IllegalStateException("The annotation did not contain a field named " + field));
    }

    public AnnotationMetaData requireAnnotation(String field) {
        return getAnnotation(field).orElseThrow(() -> new IllegalStateException("The annotation did not contain a field named " + field));
    }

    public boolean isOfType(Class<? extends Annotation> annotationType) {
        return annotationType.getName().equals(className);
    }

    public String className() {
        return className;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnnotationMetaData that = (AnnotationMetaData) o;
        return Objects.equals(fields, that.fields) && Objects.equals(className, that.className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fields, className);
    }

    @Override
    public String toString() {
        return className + "(" + fields + ")";
    }

    public boolean isEmpty() {
        return fields.isEmpty();
    }

    public static class Builder {

        private final Map<String, Object> fields = new HashMap<>();
        private final String className;

        public Builder(String className) {
            this.className = className;
        }

        public Builder withField(String field, String value) {
            this.fields.put(field, value);
            return this;
        }

        public Builder withField(String field, Boolean value) {
            this.fields.put(field, value);
            return this;
        }

        public Builder withField(String field, Integer value) {
            this.fields.put(field, value);
            return this;
        }

        public Builder withField(String field, Long value) {
            this.fields.put(field, value);
            return this;
        }

        public Builder withField(String field, Float value) {
            this.fields.put(field, value);
            return this;
        }

        public Builder withField(String field, Double value) {
            this.fields.put(field, value);
            return this;
        }

        public Builder withField(String field, Class<?> value) {
            this.fields.put(field, value);
            return this;
        }

        public <T extends Enum<T>> Builder withEnum(String field, T enumValue) {
            this.fields.put(field, enumValue);
            return this;
        }

        public Builder withAnnotation(String field, Consumer<Builder> consumer) {
            Builder builder = new Builder(className);
            consumer.accept(builder);
            this.fields.put(field, builder.build());
            return this;
        }

        public Builder withAnnotation(String field, AnnotationMetaData annotation) {
            this.fields.put(field, annotation);
            return this;
        }

        public Builder resolveField(String field, Object value) {
            Class<?> type = typeOf(value);

            if (type == AnnotationMirror.class) {
                withAnnotation(field, AnnotationMetaData.of((AnnotationMirror) value));
            } else {
                this.fields.put(field, value);
            }
            return this;
        }

        private Class<?> typeOf(Object o) {
            Class<?> type = o.getClass();
            if (type.isPrimitive()) {
                return Primitives.wrap(type);
            }

            return type;
        }

        public AnnotationMetaData build() {
            AnnotationMetaData result = new AnnotationMetaData(className, new HashMap<>(fields));
            fields.clear();
            return result;
        }
    }
}
