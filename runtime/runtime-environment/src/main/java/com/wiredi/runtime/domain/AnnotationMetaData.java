package com.wiredi.runtime.domain;

import com.google.common.primitives.Primitives;
import com.wiredi.logging.Logging;
import com.wiredi.runtime.collections.EnumSet;
import com.wiredi.runtime.domain.provider.TypeIdentifier;

import javax.lang.model.element.AnnotationMirror;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AnnotationMetaData {

    private static final Logging LOGGER = Logging.getInstance(AnnotationMetaData.class);
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

    public static Builder newInstance(Class<?> type) {
        return new Builder(type.getName());
    }

    public static AnnotationMetaData none() {
        return empty("");
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

    // ########### Accessors ###########

    public Optional<String> get(String field) {
        Object value = fields.get(field);
        if (value == null) {
            return Optional.empty();
        }

        if (value instanceof String s) {
            return Optional.of(s);
        }

        LOGGER.warn("Tried to access annotation value " + field + " from annotation " + className + ". Expected a String, but got the value " + value);
        return Optional.empty();
    }

    public String get(String field, String alternative) {
        Object value = fields.get(field);
        if (value == null) {
            return alternative;
        }

        if (value instanceof String) {
            return (String) value;
        }

        LOGGER.warn("Tried to access annotation value " + field + " from annotation " + className + ". Expected a String, but got the value " + value);
        return alternative;
    }

    public Optional<Boolean> getBoolean(String field) {
        Object value = fields.get(field);
        if (value == null) {
            return Optional.empty();
        }

        if (value instanceof Boolean b) {
            return Optional.of(b);
        } else if (value instanceof String s) {
            return Optional.of(s).map(Boolean::valueOf);
        }

        LOGGER.warn("Tried to access annotation value " + field + " from annotation " + className + ". Expected a boolean, but got the value " + value);
        return Optional.empty();
    }

    public boolean getBoolean(String field, boolean alternative) {
        Object value = fields.get(field);
        if (value == null) {
            return alternative;
        }

        if (value instanceof Boolean b) {
            return b;
        } else if (value instanceof String s) {
            return Boolean.parseBoolean(s);
        }

        LOGGER.warn("Tried to access annotation value " + field + " from annotation " + className + ". Expected a boolean, but got the value " + value);
        return alternative;
    }

    public Optional<Integer> getInt(String field) {
        Object value = fields.get(field);
        if (value == null) {
            return Optional.empty();
        }

        if (value instanceof Integer) {
            return Optional.ofNullable((Integer) value);
        } else if (value instanceof String) {
            return Optional.ofNullable((String) value).map(Integer::valueOf);
        }

        LOGGER.warn("Tried to access annotation value " + field + " from annotation " + className + ". Expected an integer, but got the value " + value);
        return Optional.empty();
    }

    public int getInt(String field, int alternative) {
        Object value = fields.get(field);
        if (value == null) {
            return alternative;
        }

        if (value instanceof Integer i) {
            return i;
        } else if (value instanceof String s) {
            return Integer.parseInt(s);
        }

        LOGGER.warn("Tried to access annotation value " + field + " from annotation " + className + ". Expected an integer, but got the value " + value);
        return alternative;
    }

    public Optional<Long> getLong(String field) {
        Object value = fields.get(field);
        if (value == null) {
            return Optional.empty();
        }

        if (value instanceof Long l) {
            return Optional.of(l);
        } else if (value instanceof String s) {
            return Optional.of(Long.parseLong(s));
        }

        LOGGER.warn("Tried to access annotation value " + field + " from annotation " + className + ". Expected a long, but got the value " + value);
        return Optional.empty();
    }

    public long getLong(String field, long alternative) {
        Object value = fields.get(field);
        if (value == null) {
            return alternative;
        }

        if (value instanceof Long l) {
            return l;
        } else if (value instanceof String s) {
            return Long.parseLong(s);
        }

        LOGGER.warn("Tried to access annotation value " + field + " from annotation " + className + ". Expected a long, but got the value " + value);
        return alternative;
    }

    public Optional<Float> getFloat(String field) {
        Object value = fields.get(field);
        if (value == null) {
            return Optional.empty();
        }

        if (value instanceof Float f) {
            return Optional.of(f);
        } else if (value instanceof String s) {
            return Optional.of(Float.parseFloat(s));
        }

        LOGGER.warn("Tried to access annotation value " + field + " from annotation " + className + ". Expected a float, but got the value " + value);
        return Optional.empty();
    }

    public float getFloat(String field, float alternative) {
        Object value = fields.get(field);
        if (value == null) {
            return alternative;
        }

        if (value instanceof Float f) {
            return f;
        } else if (value instanceof String s) {
            return Float.parseFloat(s);
        }

        LOGGER.warn("Tried to access annotation value " + field + " from annotation " + className + ". Expected a float, but got the value " + value);
        return alternative;
    }

    public Optional<Double> getDouble(String field) {
        Object value = fields.get(field);
        if (value == null) {
            return Optional.empty();
        }

        if (value instanceof Double d) {
            return Optional.of(d);
        } else if (value instanceof String s) {
            return Optional.of(Double.parseDouble(s));
        }

        LOGGER.warn("Tried to access annotation value " + field + " from annotation " + className + ". Expected a double, but got the value " + value);
        return Optional.empty();
    }

    public double getDouble(String field, double alternative) {
        Object value = fields.get(field);
        if (value == null) {
            return alternative;
        }

        if (value instanceof Double d) {
            return d;
        } else if (value instanceof String s) {
            return Double.parseDouble(s);
        }

        LOGGER.warn("Tried to access annotation value " + field + " from annotation " + className + ". Expected a double, but got the value " + value);
        return alternative;
    }

    public Optional<Class<?>> getClass(String field) {
        Object value = fields.get(field);
        if (value == null) {
            return Optional.empty();
        }

        if (value instanceof Class<?> c) {
            return Optional.of(c);
        } else if (value instanceof String s) {
            return Optional.of(s).map(rawValue -> {
                try {
                    return Class.forName(rawValue);
                } catch (ClassNotFoundException e) {
                    LOGGER.warn("Tried to access annotation value " + field + " from annotation " + className + " as a class. Actual value was a string and parsing the class name failed.", e);
                    return null;
                }
            });
        }

        LOGGER.warn("Tried to access annotation value " + field + " from annotation " + className + ". Expected a class, but got the value " + value);
        return Optional.empty();
    }

    public Class<?> getClass(String field, Class<?> alternative) {
        Object value = fields.get(field);
        if (value == null) {
            return alternative;
        }

        if (value instanceof Class<?> c) {
            return c;
        } else if (value instanceof String s) {
            try {
                return Class.forName(s);
            } catch (ClassNotFoundException e) {
                LOGGER.warn("Tried to access annotation value " + field + " from annotation " + className + " as a class. Actual value was a string and parsing the class name failed.", e);
                return null;
            }
        }

        LOGGER.warn("Tried to access annotation value " + field + " from annotation " + className + ". Expected a class, but got the value " + value);
        return alternative;
    }

    public <T extends Enum<T>> Optional<T> getEnum(String field, Class<T> enumType) {
        Object value = fields.get(field);
        if (value == null) {
            return Optional.empty();
        }

        if (enumType.isAssignableFrom(value.getClass())) {
            return Optional.ofNullable((T) value);
        } else if (value instanceof String) {
            return Optional.ofNullable((String) value).map(fieldValue -> EnumSet.of(enumType).require(fieldValue));
        }

        LOGGER.warn("Tried to access annotation value " + field + " from annotation " + className + ". Expected a nested annotation, but got the value " + value);
        return Optional.empty();
    }

    public <T extends Enum<T>> T getEnum(String field, T alternative) {
        Object value = fields.get(field);
        if (value == null) {
            return alternative;
        }

        if (alternative.getClass().isAssignableFrom(value.getClass())) {
            return (T) value;
        } else if (value instanceof String s) {
            return (T) EnumSet.of(alternative.getClass()).require(s);
        }

        LOGGER.warn("Tried to access annotation value " + field + " from annotation " + className + ". Expected a nested annotation, but got the value " + value);
        return alternative;
    }

    public Optional<AnnotationMetaData> getAnnotation(String field) {
        Object value = fields.get(field);
        if (value == null) {
            return Optional.empty();
        }

        if (value instanceof AnnotationMetaData) {
            return Optional.ofNullable((AnnotationMetaData) value);
        }

        LOGGER.warn("Tried to access annotation value " + field + " from annotation " + className + ". Expected a nested annotation, but got the value " + value);
        return Optional.empty();
    }

    public AnnotationMetaData getAnnotation(String field, AnnotationMetaData alternative) {
        Object value = fields.get(field);
        if (value == null) {
            return alternative;
        }

        if (value instanceof AnnotationMetaData a) {
            return a;
        }

        LOGGER.warn("Tried to access annotation value " + field + " from annotation " + className + ". Expected a nested annotation, but got the value " + value);
        return alternative;
    }

    public void forEach(BiConsumer<String, Object> consumer) {
        fields.forEach(consumer);
    }

    public Optional<TypeIdentifier<?>> getType(String field) {
        return getClass(field).map(TypeIdentifier::just);
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
