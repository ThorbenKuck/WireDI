package com.wiredi.properties;

import com.wiredi.properties.exceptions.PropertyNotFoundException;
import com.wiredi.properties.keys.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A property representation, that is supporting strong types.
 * <p>
 * Properties are taken in different priorities from sources.
 * <p>
 * First this class checks if the environment contains the property. If not, the System properties are checked.
 * If neither the environment, nor the System contain the property, it will be taken from the local cache.
 */
public final class TypedProperties implements AutoCloseable {

    public static final Key RESPECT_ENVIRONMENT_PROPERTY_KEY = Key.just("wire-di.typed-properties.respect-environment");
    private static final boolean GLOBAL_RESPECT_ENVIRONMENT;

    static {
        GLOBAL_RESPECT_ENVIRONMENT = takeFromEnvironment(RESPECT_ENVIRONMENT_PROPERTY_KEY)
                .map(Boolean::parseBoolean)
                .orElse(true);
    }

    @NotNull
    private final Map<Key, String> properties = new HashMap<>();
    private boolean respectEnvironment = GLOBAL_RESPECT_ENVIRONMENT;

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
    private static Optional<String> takeFromEnvironment(@NotNull final Key key) {
        final String formattedKey = key.value();
        return Optional.ofNullable(System.getenv(formattedKey))
                .or(() -> Optional.ofNullable(System.getProperty(formattedKey)));
    }

    private static Supplier<PropertyNotFoundException> notFound(Key key) {
        return () -> new PropertyNotFoundException(key.value());
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
    public TypedProperties set(
            @NotNull final Key key,
            @NotNull final String value
    ) {
        if (respectEnvironment) {
            String result = takeFromEnvironment(key).orElse(value);
            properties.put(key, result);
        } else {
            properties.put(key, value);
        }
        return this;
    }

    @NotNull
    public TypedProperties set(
            @NotNull final Key key,
            final boolean value
    ) {
        return set(key, Boolean.toString(value));
    }

    @NotNull
    public TypedProperties set(
            @NotNull final Key key,
            final int value
    ) {
        return set(key, Integer.toString(value));
    }

    @NotNull
    public TypedProperties respectEnvironment(final boolean value) {
        this.respectEnvironment = value;
        return this;
    }

    /* Reading options */

    @NotNull
    public TypedProperties set(
            @NotNull final Key key,
            final float value
    ) {
        return set(key, Float.toString(value));
    }

    @NotNull
    public TypedProperties set(
            @NotNull final Key key,
            final double value
    ) {
        return set(key, Double.toString(value));
    }

    @NotNull
    public TypedProperties clear() {
        properties.clear();
        return this;
    }

    public boolean contains(
            @NotNull final Key key
    ) {
        return properties.containsKey(key);
    }

    /**
     * The primary reading info for reading a property.
     * <p>
     * The access will use a read lock.
     *
     * @param key the key to look up
     * @return the stored value as an optional
     */
    @NotNull
    private Optional<String> doGet(@NotNull final Key key) {
        return Optional.ofNullable(this.properties.get(key));
    }

    @NotNull
    public <T> Optional<T> getTyped(
            @NotNull final Key key,
            @NotNull final Class<T> type
    ) {
        return get(key).map(it -> TypeMapper.convert(type, key, it));
    }

    @NotNull
    public <T> T requireTyped(
            @NotNull final Key key,
            @NotNull final Class<T> type
    ) {
        return get(key).map(it -> TypeMapper.convert(type, key, it)).orElseThrow(notFound(key));
    }

    @NotNull
    public <T> T getTyped(
            @NotNull final Key key,
            @NotNull final Class<T> type,
            @NotNull final String defaultValue
    ) {
        return Optional.ofNullable(get(key, defaultValue))
                .map(it -> TypeMapper.convert(type, key, it))
                .orElseThrow(notFound(key));
    }

    @Nullable
    public Integer getInt(@NotNull final Key key) {
        return getTyped(key, int.class)
                .orElse(null);
    }

    @NotNull
    public Integer requireInt(@NotNull final Key key) {
        return getTyped(key, int.class).orElseThrow(notFound(key));
    }

    @NotNull
    public Integer getInt(
            @NotNull final Key key,
            final int defaultValue
    ) {
        return getTyped(key, int.class, Integer.toString(defaultValue));
    }

    @Nullable
    public Float getFloat(@NotNull final Key key) {
        return getTyped(key, float.class).orElse(null);
    }

    @NotNull
    public Float requireFloat(@NotNull final Key key) {
        return getTyped(key, float.class).orElseThrow(notFound(key));
    }

    @NotNull
    public Float getFloat(
            @NotNull final Key key,
            final float defaultValue
    ) {
        return getTyped(key, float.class, Float.toString(defaultValue));
    }

    @Nullable
    public Double getDouble(@NotNull final Key key) {
        return getTyped(key, double.class).orElse(null);
    }

    @NotNull
    public Double requireDouble(@NotNull final Key key) {
        return getTyped(key, double.class).orElseThrow(notFound(key));
    }

    @NotNull
    public Double getDouble(
            @NotNull final Key key,
            final double defaultValue
    ) {
        return getTyped(key, double.class, Double.toString(defaultValue));
    }

    @Nullable
    public Boolean getBoolean(@NotNull final Key key) {
        return getTyped(key, boolean.class).orElse(null);
    }

    @NotNull
    public Boolean requireBoolean(@NotNull final Key key) {
        return getTyped(key, boolean.class).orElseThrow(notFound(key));
    }

    @NotNull
    public Boolean getBoolean(
            @NotNull final Key key,
            final boolean defaultValue
    ) {
        return getTyped(key, boolean.class, Boolean.toString(defaultValue));
    }

    @NotNull
    public String get(
            @NotNull final Key key,
            @NotNull final String defaultValue
    ) {
        return doGet(key).orElse(defaultValue);
    }

    @NotNull
    public String require(@NotNull final Key key) {
        return get(key).orElseThrow(notFound(key));
    }

    @NotNull
    public Optional<String> get(@NotNull final Key key) {
        return doGet(key);
    }

    @NotNull
    public List<String> getAll(@NotNull final Key key) {
        return getAll(key, Collections.emptyList());
    }

    @NotNull
    public List<String> getAll(@NotNull final Key key, List<String> defaultValue) {
        return get(key)
                .map(it -> Arrays.asList(it.split(",")))
                .orElse(defaultValue);
    }

    @NotNull
    public <T> List<T> getAll(@NotNull final Key key, @NotNull final Class<T> type) {
        return getAll(key).stream()
                .map(it -> TypeMapper.convert(type, key, it))
                .collect(Collectors.toList());
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

    public PropertyReference getReferenceFor(Key key) {
        return new PropertyReference(this, key);
    }
}
