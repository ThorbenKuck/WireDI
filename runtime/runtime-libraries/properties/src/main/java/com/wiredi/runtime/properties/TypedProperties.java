package com.wiredi.runtime.properties;

import com.wiredi.runtime.properties.exceptions.PropertyNotFoundException;
import com.wiredi.runtime.types.TypeMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A property representation, that is supporting strong types.
 * <p>
 * Properties are taken in different priorities from sources.
 * <p>
 * First this class checks if the environment contains the property. If not, the System properties are checked.
 * If neither the environment, nor the System contain the property, it will be taken from the local cache.
 */
public final class TypedProperties implements AutoCloseable {

    private static final String LIST_ENTRY_SEPARATOR = ",";

    @NotNull
    private final Map<Key, String> properties = new HashMap<>();
    private final TypeMapper typeMapper;

    public TypedProperties() {
        this(TypeMapper.getInstance());
    }

    public TypedProperties(TypeMapper typeMapper) {
        this.typeMapper = typeMapper;
    }

    @NotNull
    public static TypedProperties from(@NotNull final Map<Key, String> rawProperties) {
        final TypedProperties result = new TypedProperties();
        result.setAll(rawProperties);
        return result;
    }

    @NotNull
    public static TypedProperties from(@NotNull final TypedProperties typedProperties) {
        final TypedProperties result = new TypedProperties();
        result.setAll(typedProperties.properties);
        return result;
    }

    @NotNull
    public static TypedProperties from(@NotNull final Properties properties) {
        final TypedProperties result = new TypedProperties();
        result.setAll(properties);
        return result;
    }

    private static Supplier<PropertyNotFoundException> notFound(Key key) {
        return () -> new PropertyNotFoundException(key.value());
    }

    public TypeMapper typeMapper() {
        return typeMapper;
    }

    /* Modifying options */

    @NotNull
    public TypedProperties setAll(@NotNull final Map<Key, String> rawProperties) {
        properties.putAll(rawProperties);
        return this;
    }

    @NotNull
    public TypedProperties setAll(@NotNull final TypedProperties typedProperties) {
        properties.putAll(typedProperties.properties);
        return this;
    }

    @NotNull
    public TypedProperties setAll(@NotNull final Properties properties) {
        properties.stringPropertyNames().forEach(name -> properties.put(name, properties.getProperty(name)));
        return this;
    }

    @NotNull
    public TypedProperties add(
            @NotNull final Key key,
            @NotNull final Object value
    ) {
        return add(key, typeMapper.convert(value, String.class));
    }

    @NotNull
    public TypedProperties add(
            @NotNull final Key key,
            @NotNull final String value
    ) {
        properties.merge(key, value, (a, b) -> a + LIST_ENTRY_SEPARATOR + b);
        return this;
    }

    @NotNull
    public TypedProperties set(
            @NotNull final Key key,
            final Object value
    ) {
        return set(key, typeMapper.convert(value, String.class));
    }

    @NotNull
    public TypedProperties set(
            @NotNull final Key key,
            final String value
    ) {
        properties.put(key, typeMapper.convert(value, String.class));
        return this;
    }

    @NotNull
    public TypedProperties clear() {
        properties.clear();
        return this;
    }

    /* Reading options */
    public boolean contains(
            @NotNull final Key key
    ) {
        return properties.containsKey(key);
    }

    public int size() {
        return properties.size();
    }

    /**
     * The primary reading info for reading a property with support for Optional.
     *
     * @param key the key to look up
     * @return the stored value as an optional
     */
    @NotNull
    private Optional<String> doGetOptional(@NotNull final Key key) {
        return Optional.ofNullable(this.properties.get(key));
    }

    /**
     * The primary reading info for reading a property.
     *
     * @param key the key to look up
     * @return the stored value
     */
    @Nullable
    private String doGetNullable(@NotNull final Key key) {
        return this.properties.get(key);
    }

    @NotNull
    public <T> T require(
            @NotNull final Key key,
            @NotNull final Class<T> type
    ) {
        String raw = doGetNullable(key);
        if (raw == null) {
            throw notFound(key).get();
        }
        return typeMapper.convert(raw, type);
    }

    @NotNull
    public <T> Optional<T> tryGet(
            @NotNull final Key key,
            @NotNull final Class<T> type
    ) {
        return doGetOptional(key).map(it -> typeMapper.convert(it, type));
    }

    @NotNull
    public <T, S extends T> T get(
            @NotNull final Key key,
            @NotNull final Class<T> type,
            @NotNull final Supplier<S> defaultValue
    ) {
        String raw = doGetNullable(key);
        if (raw == null) {
            return defaultValue.get();
        } else {
            return typeMapper.convert(raw, type);
        }
    }

    @NotNull
    public <T, S extends T> T get(
            @NotNull final Key key,
            @NotNull final Class<T> type,
            @NotNull final String rawDefault
    ) {
        String raw = doGetNullable(key);
        return typeMapper.convert(Objects.requireNonNullElse(raw, rawDefault), type);
    }

    public <T> Optional<T> get(Key key, @NotNull final Class<T> type) {
        return doGetOptional(key).map(it -> typeMapper.convert(it, type));
    }

    @NotNull
    public <T, S extends T> T get(
            @NotNull final Key key,
            @NotNull final Class<T> type,
            @NotNull final S defaultValue
    ) {
        String raw = doGetNullable(key);
        if (raw == null) {
            return defaultValue;
        } else {
            return typeMapper.convert(raw, type);
        }
    }

    @Nullable
    public Integer getInt(@NotNull final Key key) {
        return tryGet(key, int.class)
                .orElse(null);
    }

    @NotNull
    public Integer requireInt(@NotNull final Key key) {
        return tryGet(key, int.class).orElseThrow(notFound(key));
    }

    public int getInt(
            @NotNull final Key key,
            final int defaultValue
    ) {
        String raw = doGetNullable(key);
        if (raw == null) {
            return defaultValue;
        }
        return typeMapper.convert(raw, int.class);
    }

    @Nullable
    public Float getFloat(@NotNull final Key key) {
        return tryGet(key, float.class).orElse(null);
    }

    @NotNull
    public Float requireFloat(@NotNull final Key key) {
        return tryGet(key, float.class).orElseThrow(notFound(key));
    }

    public float getFloat(
            @NotNull final Key key,
            final float defaultValue
    ) {
        String raw = doGetNullable(key);
        if (raw == null) {
            return defaultValue;
        }
        return typeMapper.convert(raw, float.class);
    }

    @Nullable
    public Double getDouble(@NotNull final Key key) {
        return tryGet(key, double.class).orElse(null);
    }

    @NotNull
    public Double requireDouble(@NotNull final Key key) {
        return tryGet(key, double.class).orElseThrow(notFound(key));
    }

    @NotNull
    public Double getDouble(
            @NotNull final Key key,
            final double defaultValue
    ) {
        String raw = doGetNullable(key);
        if (raw == null) {
            return defaultValue;
        }
        return typeMapper.convert(raw, double.class);
    }

    @Nullable
    public Boolean getBoolean(@NotNull final Key key) {
        return tryGet(key, boolean.class).orElse(null);
    }

    @NotNull
    public Boolean requireBoolean(@NotNull final Key key) {
        return tryGet(key, boolean.class).orElseThrow(notFound(key));
    }

    @NotNull
    public Boolean getBoolean(
            @NotNull final Key key,
            final boolean defaultValue
    ) {
        String raw = doGetNullable(key);
        if (raw == null) {
            return defaultValue;
        }
        return typeMapper.convert(raw, boolean.class);
    }

    @NotNull
    public String get(
            @NotNull final Key key,
            @NotNull final String defaultValue
    ) {
        return doGetOptional(key).orElse(defaultValue);
    }

    @NotNull
    public String require(@NotNull final Key key) {
        return get(key).orElseThrow(notFound(key));
    }

    @NotNull
    public Optional<String> get(@NotNull final Key key) {
        return doGetOptional(key);
    }

    @Nullable
    public String tryGet(@NotNull final Key key) {
        return doGetNullable(key);
    }

    @NotNull
    public List<String> getAll(@NotNull final Key key) {
        return getAll(key, Collections.emptyList());
    }

    @NotNull
    public List<String> getAll(@NotNull final Key key, @NotNull final List<String> defaultValue) {
        String property = doGetNullable(key);
        if (property == null) {
            return defaultValue;
        }
        return Arrays.asList(property.split(LIST_ENTRY_SEPARATOR));
    }

    @NotNull
    public List<String> getAll(@NotNull final Key key, @NotNull final Supplier<List<String>> defaultValue) {
        String property = doGetNullable(key);
        if (property == null) {
            return defaultValue.get();
        }
        return Arrays.asList(property.split(LIST_ENTRY_SEPARATOR));
    }

    @NotNull
    public <T> List<T> getAll(@NotNull final Key key, @NotNull Function<String, T> mappingFunction) {
        return getAll(key).stream()
                .map(mappingFunction)
                .toList();
    }

    @NotNull
    public <T> List<T> getAll(@NotNull final Key key, @NotNull final Class<T> type) {
        return getAll(key, value -> typeMapper.convert(value, type));
    }

    @NotNull
    public <T> List<T> getAll(@NotNull final Key key, @NotNull final Class<T> type, final List<T> defaultValue) {
        String property = doGetNullable(key);
        if (property == null) {
            return defaultValue;
        }

        return getAll(key, value -> typeMapper.convert(value, type));
    }

    @NotNull
    public List<Boolean> getAllAsBoolean(@NotNull final Key key) {
        return getAll(key, boolean.class);
    }

    @NotNull
    public List<Integer> getAllAsInt(@NotNull final Key key) {
        return getAll(key, int.class);
    }

    @NotNull
    public List<Float> getAllAsFloat(@NotNull final Key key) {
        return getAll(key, float.class);
    }

    @NotNull
    public List<Double> getAllAsDouble(@NotNull final Key key) {
        return getAll(key, double.class);
    }

    @Override
    public void close() {
        clear();
    }

    public TypedProperties copy() {
        TypedProperties copiedInstance = new TypedProperties();
        copiedInstance.setAll(this);
        return copiedInstance;
    }

    public ThreadLocalTypedProperties threadLocal() {
        return new ThreadLocalTypedProperties(this);
    }
}
