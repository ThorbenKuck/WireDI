package com.wiredi.runtime.domain.annotations;

import com.google.common.primitives.Primitives;
import com.wiredi.annotations.stereotypes.AliasFor;
import com.wiredi.logging.Logging;
import com.wiredi.runtime.collections.EnumSet;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AnnotationMetadata {

    private static final Logging LOGGER = Logging.getInstance(AnnotationMetadata.class);
    @NotNull
    private final Map<@NotNull String, @Nullable Object> fields;
    @NotNull
    private final String className;

    public AnnotationMetadata(@NotNull String className, @NotNull Map<@NotNull String, @Nullable Object> fields) {
        this.fields = fields;
        this.className = className;
    }

    // ########### Builder ###########
    @NotNull
    public static Builder builder(@NotNull String className) {
        return new Builder(className);
    }

    @NotNull
    public static Builder builder(@NotNull Class<?> type) {
        return new Builder(type.getName());
    }

    @NotNull
    public static AnnotationMetadata none() {
        return empty("");
    }

    @NotNull
    public static AnnotationMetadata empty(@NotNull String className) {
        return new AnnotationMetadata(className, Collections.emptyMap());
    }

    @NotNull
    public static AnnotationMetadata of(@NotNull AnnotationMirror mirror) {
        Builder builder = builder(mirror.getAnnotationType().asElement().toString());
        mirror.getElementValues().forEach((method, annotationValue) -> {
            Object fieldValue = annotationValue.getValue();
            final String field = method.getSimpleName().toString();
            if (AnnotationMirror.class.isAssignableFrom(fieldValue.getClass())) {
                builder.fields.put(field, AnnotationMetadata.of((AnnotationMirror) fieldValue));
            } else {
                builder.fields.put(field, fieldValue);
            }
        });

        mirror.getElementValues().forEach((method, annotationValue) -> {
            AliasFor annotation = method.getAnnotation(AliasFor.class);
            if (annotation != null) {
                Class<?> nullType;

                try {
                    nullType = annotation.nullType();
                } catch (MirroredTypeException e) {
                    try {
                        nullType = Class.forName(e.getTypeMirror().toString());
                    } catch (ClassNotFoundException ex) {
                        throw new IllegalArgumentException(ex.getMessage(), ex);
                    }
                }

                String aliasFieldName = annotation.value();
                Object aliasValue = builder.fields.get(aliasFieldName);
                String fieldName = method.getSimpleName().toString();
                Object fieldValue = annotationValue.getValue();

                if (!fieldValue.equals(aliasValue)) {
                    if (!isNullType(fieldValue, nullType) && !isNullType(aliasValue, nullType)) {
                        throw new IllegalArgumentException("The instance " + mirror.getAnnotationType() + " has multiple aliases for the field " + fieldName + ", both are not null and not the same. Field: " + fieldName + ", Alias: " + aliasFieldName + ". Value: " + fieldValue + ", AliasValue: " + aliasValue + ".");
                    }

                    if (isNullType(aliasValue, nullType) && !isNullType(fieldValue, nullType)) {
                        builder.fields.put(aliasFieldName, fieldValue);
                    }
                    if (isNullType(fieldValue, nullType) && !isNullType(aliasValue, nullType)) {
                        builder.fields.put(fieldName, aliasValue);
                    }
                }
            }
        });
        return builder.build();
    }

    private static boolean isNullType(Object value, Class<?> nullType) {
        return switch (value) {
            case null -> true;
            case String s -> s.isBlank();
            case Class<?> c -> c.getName().equals(nullType.getName());
            case Collection<?> c -> c.isEmpty();
            case Map<?, ?> m -> m.isEmpty();
            case Integer i -> i == Integer.MIN_VALUE;
            case Long i -> i == Long.MIN_VALUE;
            case Double i -> i == Double.MIN_VALUE;
            case Float i -> i == Float.MIN_VALUE;
            default -> false;
        };
    }

    @NotNull
    public static <T extends Annotation> AnnotationMetadata of(@NotNull T annotation) {
        Class<? extends Annotation> annotationType = annotation.getClass();
        Builder builder = builder(annotationType.getName());

        Arrays.stream(annotationType.getDeclaredFields()).forEach(field -> {
            Object fieldValue;
            try {
                field.trySetAccessible();
                fieldValue = field.get(annotation);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
            if (field.getType().isAnnotation()) {
                builder.fields.put(field.getName(), AnnotationMetadata.of((AnnotationMirror) fieldValue));
            } else {
                builder.fields.put(field.getName(), fieldValue);
            }
        });

        return builder.build();
    }

    // ########### Accessors ###########
    @NotNull
    public Optional<String> get(@NotNull String field) {
        Object value = fields.get(field);
        if (value == null) {
            return Optional.empty();
        }

        if (value instanceof String s) {
            return Optional.of(s);
        }

        LOGGER.warn("Tried to access " + className + "#" + field + ". Expected a String, but got " + value.getClass() + ": " + value);
        return Optional.empty();
    }

    @NotNull
    public String get(@NotNull String field, @NotNull String alternative) {
        return get(field).orElse(alternative);
    }

    @NotNull
    public Optional<Boolean> getBoolean(@NotNull String field) {
        Object value = fields.get(field);
        if (value == null) {
            return Optional.empty();
        }

        Optional<Boolean> result = switch (value) {
            case Boolean b -> Optional.of(b);
            case String s -> Optional.of(s).map(Boolean::valueOf);
            default -> Optional.empty();
        };

        if (result.isEmpty()) {
            LOGGER.warn("Tried to access " + className + "#" + field + ". Expected a boolean, but got " + value.getClass() + ": " + value);
        }

        return result;
    }

    public boolean getBoolean(@NotNull String field, boolean alternative) {
        return getBoolean(field).orElse(alternative);
    }

    public Optional<Integer> getInt(String field) {
        Object value = fields.get(field);
        if (value == null) {
            return Optional.empty();
        }

        Optional<Integer> result = switch (value) {
            case Integer i -> Optional.of(i);
            case String s -> Optional.of(s).map(Integer::valueOf);
            default -> Optional.empty();
        };

        if (result.isEmpty()) {
            LOGGER.warn("Tried to access " + className + "#" + field + ". Expected an integer, but got " + value.getClass() + ": " + value);
        }
        return result;
    }

    public int getInt(String field, int alternative) {
        return getInt(field).orElse(alternative);
    }

    public Optional<Long> getLong(String field) {
        Object value = fields.get(field);
        if (value == null) {
            return Optional.empty();
        }

        Optional<Long> result = switch (value) {
            case Long i -> Optional.of(i);
            case String s -> Optional.of(s).map(Long::parseLong);
            default -> Optional.empty();
        };

        if (result.isEmpty()) {
            LOGGER.warn("Tried to access " + className + "#" + field + ". Expected an integer, but got " + value.getClass() + ": " + value);
        }

        return result;
    }

    public long getLong(String field, long alternative) {
        return getLong(field).orElse(alternative);
    }

    public Optional<Float> getFloat(String field) {
        Object value = fields.get(field);
        if (value == null) {
            return Optional.empty();
        }

        Optional<Float> result = switch (value) {
            case Float i -> Optional.of(i);
            case String s -> Optional.of(s).map(Float::parseFloat);
            default -> Optional.empty();
        };

        if (result.isEmpty()) {
            LOGGER.warn("Tried to access " + className + "#" + field + ". Expected a float, but got " + value.getClass() + ": " + value);
        }

        return result;
    }

    public float getFloat(String field, float alternative) {
        return getFloat(field).orElse(alternative);
    }

    public Optional<Double> getDouble(String field) {
        Object value = fields.get(field);
        if (value == null) {
            return Optional.empty();
        }

        Optional<Double> result = switch (value) {
            case Double i -> Optional.of(i);
            case String s -> Optional.of(s).map(Double::parseDouble);
            default -> Optional.empty();
        };

        if (result.isEmpty()) {
            LOGGER.warn("Tried to access " + className + "#" + field + ". Expected a double, but got " + value.getClass() + ": " + value);
        }

        return result;
    }

    public double getDouble(String field, double alternative) {
        return getDouble(field).orElse(alternative);
    }

    public Optional<Class<?>> getRawClass(String field) {
        return getClass(field).map(it -> it);
    }

    public Class<?> getRawClass(String field, Class<?> alternative) {
        return getRawClass(field).orElse(alternative);
    }

    public <T> Optional<Class<T>> getClass(String field) {
        Object value = fields.get(field);
        if (value == null) {
            return Optional.empty();
        }

        if (value instanceof TypeMirror || value instanceof Element) {
            return Optional.of(value.toString())
                    .map(this::classFromName)
                    .map(it -> (Class<T>) it);
        }

        Optional<Class<?>> result = switch (value) {
            case Class<?> c -> Optional.of(c);
            case String s -> Optional.of(s).map(this::classFromName);
            default -> Optional.empty();
        };

        if (result.isEmpty()) {
            LOGGER.warn("Tried to access " + className + "#" + field + ". Expected a class, but got " + value.getClass() + ": " + value);
        }
        return result.map(it -> (Class<T>) it);
    }

    public <T> Class<T> getClass(String field, Class<T> alternative) {
        return this.<T>getClass(field).orElse(alternative);
    }

    public Optional<String> getTypeName(String field) {
        Object value = fields.get(field);
        if (value == null) {
            return Optional.empty();
        }

        return switch (value) {
            case Class<?> c -> Optional.of(c.getName());
            case TypeMirror tm -> Optional.of(tm.toString());
            case Element e -> Optional.of(e.toString());
            case String s -> Optional.of(s);
            default -> Optional.empty();
        };
    }

    public String getTypeName(String field, String alternative) {
        return getTypeName(field).orElse(alternative);
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

        LOGGER.warn("Tried to access " + className + "#" + field + ". Expected a nested instance, but got the value " + value);
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

        LOGGER.warn("Tried to access " + className + "#" + field + ". Expected an enum, but got " + value.getClass() + ": " + value);
        return alternative;
    }

    public Optional<AnnotationMetadata> getAnnotation(String field) {
        Object value = fields.get(field);
        if (value == null) {
            return Optional.empty();
        }

        if (value instanceof AnnotationMetadata) {
            return Optional.ofNullable((AnnotationMetadata) value);
        }

        LOGGER.warn("Tried to access " + className + "#" + field + ". Expected an annotation, but got " + value.getClass() + ": " + value);
        return Optional.empty();
    }

    public AnnotationMetadata getAnnotation(String field, AnnotationMetadata alternative) {
        Object value = fields.get(field);
        if (value == null) {
            return alternative;
        }

        if (value instanceof AnnotationMetadata a) {
            return a;
        }

        LOGGER.warn("Tried to access " + className + "#" + field + ". Expected an annotation, but got " + value.getClass() + ": " + value);
        return alternative;
    }

    public Optional<TypeIdentifier<?>> getType(String field) {
        Object value = fields.get(field);
        if (value == null) {
            return Optional.empty();
        }

        if (value instanceof TypeMirror || value instanceof Element) {
            return Optional.of(value.toString())
                    .map(this::classFromName)
                    .map(TypeIdentifier::of);
        }

        Optional<TypeIdentifier<?>> result = switch (value) {
            case Class<?> c -> Optional.of(TypeIdentifier.of(c));
            case TypeIdentifier<?> t -> Optional.of(t);
            case String s -> Optional.of(s)
                    .map(this::classFromName)
                    .map(TypeIdentifier::of);
            default -> Optional.empty();
        };

        if (result.isEmpty()) {
            LOGGER.warn("Tried to access " + className + "#" + field + ". Expected a class, but got " + value.getClass() + ": " + value);
        }
        return result;
    }

    public TypeIdentifier<?> getType(String field, TypeIdentifier<?> alternative) {
        return getType(field).orElse(alternative);
    }

    public void forEach(BiConsumer<String, Object> consumer) {
        fields.forEach(consumer);
    }

    public String require(String field) {
        return get(field).orElseThrow(() -> new IllegalStateException("The instance did not contain a field named " + field));
    }

    public Boolean requireBoolean(String field) {
        return getBoolean(field).orElseThrow(() -> new IllegalStateException("The instance did not contain a boolean field named " + field));
    }

    public Integer requireInt(String field) {
        return getInt(field).orElseThrow(() -> new IllegalStateException("The instance did not contain an int field named " + field));
    }

    public Long requireLong(String field) {
        return getLong(field).orElseThrow(() -> new IllegalStateException("The instance did not contain a long field named " + field));
    }

    public Float requireFloat(String field) {
        return getFloat(field).orElseThrow(() -> new IllegalStateException("The instance did not contain a float field named " + field));
    }

    public Double requireDouble(String field) {
        return getDouble(field).orElseThrow(() -> new IllegalStateException("The instance did not contain a double field named " + field));
    }

    public Class<?> requireRawClass(String field) {
        return this.getRawClass(field).orElseThrow(() -> new IllegalStateException("The instance did not contain a class field named " + field));
    }

    public <T> Class<T> requireClass(String field) {
        return this.<T>getClass(field).orElseThrow(() -> new IllegalStateException("The instance did not contain a class field named " + field));
    }

    public TypeIdentifier<?> requireType(String field) {
        return getType(field).orElseThrow(() -> new IllegalStateException("The instance did not contain a type field named " + field));
    }

    public <T extends Enum<T>> T requireEnum(String field, Class<T> enumType) {
        return getEnum(field, enumType).orElseThrow(() -> new IllegalStateException("The instance did not contain an enum field named " + field));
    }

    public AnnotationMetadata requireAnnotation(String field) {
        return getAnnotation(field).orElseThrow(() -> new IllegalStateException("The instance did not contain an instance field named " + field));
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
        AnnotationMetadata that = (AnnotationMetadata) o;
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

    @Nullable
    private Class<?> classFromName(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Unable to convert " + className + " as a class.", e);
            return null;
        }
    }

    public static class Builder {


        private final Map<String, Object> fields = new HashMap<>();
        private final String className;

        public Builder(String className) {
            this.className = className;
        }

        private static String getQualifiedNameFromTypeMirror(TypeMirror typeMirror) {
            if (typeMirror.getKind() != TypeKind.DECLARED) return null;
            DeclaredType declaredType = (DeclaredType) typeMirror;
            Element element = declaredType.asElement();
            if (!(element instanceof TypeElement)) return null;
            return ((TypeElement) element).getQualifiedName().toString();
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

        public Builder withField(String field, TypeElement value) {
            this.fields.put(field, value.getQualifiedName().toString());
            return this;
        }

        public Builder withField(String field, TypeMirror value) {
            this.fields.put(field, getQualifiedNameFromTypeMirror(value));
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

        public Builder withAnnotation(String field, AnnotationMetadata annotation) {
            this.fields.put(field, annotation);
            return this;
        }

        public Builder resolveField(String field, Object value) {
            Class<?> type = typeOf(value);

            if (type == AnnotationMirror.class) {
                withAnnotation(field, AnnotationMetadata.of((AnnotationMirror) value));
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

        public AnnotationMetadata build() {
            AnnotationMetadata result = new AnnotationMetadata(className, new HashMap<>(fields));
            fields.clear();
            return result;
        }
    }
}
