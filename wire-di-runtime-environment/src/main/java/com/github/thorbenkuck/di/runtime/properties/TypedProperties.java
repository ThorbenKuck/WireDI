package com.github.thorbenkuck.di.runtime.properties;

import com.github.thorbenkuck.di.lang.DataAccess;
import com.github.thorbenkuck.di.annotations.ManualWireCandidate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@ManualWireCandidate
public class TypedProperties implements AutoCloseable {

    @NotNull
    private final Map<String, String> properties = new HashMap<>();

    @NotNull
    private final DataAccess dataAccess = new DataAccess();

    @NotNull
    private static final Map<Class<?>, PropertyConverter<?>> typeMappings = new HashMap<>();

    static {
        setTypeConverter(boolean.class, TypedProperties::getBoolean);
        setTypeConverter(int.class, TypedProperties::getInt);
        setTypeConverter(float.class, TypedProperties::getFloat);
        setTypeConverter(double.class, TypedProperties::getDouble);

        setTypeConverter(Boolean.class, TypedProperties::getBoolean);
        setTypeConverter(Integer.class, TypedProperties::getInt);
        setTypeConverter(Float.class, TypedProperties::getFloat);
        setTypeConverter(Double.class, TypedProperties::getDouble);

        setTypeConverter(String.class, TypedProperties::get);
    }

    public static <T> void setTypeConverter(Class<T> type, PropertyConverter<T> converter) {
        typeMappings.put(type, converter);
    }

    public static TypedProperties fromInputStream(InputStream inputStream) {
        final TypedProperties typedProperties = new TypedProperties();
        try {
            typedProperties.loadProperties(inputStream);
        } catch (IOException e) {
            throw new PropertiesNotFoundException(e);
        }
        return typedProperties;
    }

    public static TypedProperties fromInputStreamOrEmpty(InputStream inputStream) {
        final TypedProperties typedProperties = new TypedProperties();
        try {
            typedProperties.loadProperties(inputStream);
        } catch (IOException ignored) {
        }
        return typedProperties;
    }

    @NotNull
    public static TypedProperties fromClassPath(@NotNull final String path) {
        final TypedProperties typedProperties = new TypedProperties();
        try {
            typedProperties.loadProperties(path);
        } catch (final IOException e) {
            throw new PropertiesNotFoundException(path, e);
        }
        return typedProperties;
    }

    @NotNull
    public static TypedProperties fromClassPathOrEmpty(@NotNull final String path) {
        final TypedProperties typedProperties = new TypedProperties();
        try {
            typedProperties.loadProperties(path);
        } catch (final IOException ignored) {
        }
        return typedProperties;
    }

    @NotNull
    public static TypedProperties fromString(@NotNull final String content) {
        final TypedProperties typedProperties = new TypedProperties();
        try {
            final Properties properties = parse(content);
            typedProperties.addAll(properties);
        } catch (final IOException e) {
            throw new InvalidPropertySyntaxException(content, e);
        }

        return typedProperties;
    }

    /* Modifying options */

    public void tryTakeFromEnvironment(
            @NotNull final String key,
            @NotNull final Object defaultValue
    ) {
        String formattedKey = Keys.format(key);
        if(contains(formattedKey)) {
            return;
        }

        dataAccess.write(() -> {
            if(properties.containsKey(formattedKey)) {
                return;
            }

            if(System.getProperty(formattedKey) != null) {
                this.properties.put(formattedKey, System.getProperty(formattedKey));
            } else if(System.getenv(formattedKey) != null) {
                this.properties.put(formattedKey, System.getenv(formattedKey));
            } else {
                this.properties.put(formattedKey, defaultValue.toString());
            }
        });
    }

    public void set(
            @NotNull final String key,
            @NotNull final String value
    ) {
        dataAccess.write(() -> properties.put(Keys.format(key), value));
    }

    public void set(
            @NotNull final String key,
            final boolean value
    ) {
        set(key, Boolean.toString(value));
    }

    public void set(
            @NotNull final String key,
            final int value
    ) {
        set(key, Integer.toString(value));
    }

    public void set(
            @NotNull final String key,
            final float value
    ) {
        set(key, Float.toString(value));
    }

    public void set(
            @NotNull final String key,
            final double value
    ) {
        set(key, Double.toString(value));
    }

    public void addAll(@NotNull final Properties newProperties) {
        dataAccess.write(() -> {
            newProperties.stringPropertyNames().forEach(key -> {
                String value = newProperties.getProperty(key);
                String formattedKey = Keys.format(key);
                this.properties.put(formattedKey, value);
            });
        });
    }

    public void addAll(@NotNull final TypedProperties typedProperties) {
        dataAccess.write(() -> {
            properties.putAll(typedProperties.properties);
        });
    }

    public void loadProperties(@NotNull final String file) throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(file);
        if(stream == null) {
            stream = getClass().getClassLoader().getResourceAsStream("/" + file);
        }

        loadProperties(stream);
    }

    public void loadProperties(@NotNull final File file) throws IOException {
        loadProperties(file.toPath());
    }

    public void loadProperties(@NotNull final Path path) throws IOException {
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("The provided file " + path + " is not a file, but it should be one.");
        }
        loadProperties(Files.newInputStream(path));
    }

    public void loadProperties(final InputStream inputStream) throws IOException {
        if(inputStream == null) {
            throw new IOException("InputStream could not be loaded");
        }
        try {
            final Properties properties = new Properties();
            properties.load(inputStream);
            addAll(properties);
            properties.clear(); // Help GC and loos value references
        } finally {
            inputStream.close();
        }
    }

    public void clear() {
        dataAccess.write(properties::clear);
    }

    public boolean contains(String key) {
        return dataAccess.read(() -> properties.containsKey(key));
    }

    /* Reading options */

    @NotNull
    public <T> T getTyped(
            @NotNull final String key,
            @NotNull final Class<T> type
    ) {
        return getTyped(key, type, null);
    }

    @NotNull
    public <T> T getTyped(
            @NotNull final String key,
            @NotNull final Class<T> type,
            @Nullable final String defaultValue
    ) {
        final PropertyConverter<?> typedProvider = typeMappings.get(type);
        if (typedProvider == null) {
            throw new IllegalArgumentException("Unknown type " + type + " for the TypedProperties!");
        }

        T result = (T) typedProvider.apply(this, key, defaultValue);
        if (result == null) {
            throw new PropertyNotFoundException(key);
        }
        return result;
    }

    @NotNull
    public Integer getInt(@NotNull final String key) {
        String value = get(key);
        return asInt(key, value);
    }

    @NotNull
    public Integer getInt(
            @NotNull final String key,
            @Nullable final String defaultValue
    ) {
        String value = get(key, defaultValue);
        return asInt(key, value);
    }

    @NotNull
    public Integer getInt(
            @NotNull final String key,
            final int defaultValue
    ) {
        return getInt(key, Integer.toString(defaultValue));
    }

    @NotNull
    public Float getFloat(@NotNull final String key) {
        String value = get(key);
        return asFloat(key, value);
    }

    @NotNull
    public Float getFloat(
            @NotNull final String key,
            @Nullable final String defaultValue
    ) {
        String value = get(key, defaultValue);
        return asFloat(key, value);
    }

    @NotNull
    public Float getFloat(
            @NotNull final String key,
            final float defaultValue
    ) {
        return getFloat(key, Float.toString(defaultValue));
    }

    @NotNull
    public Double getDouble(@NotNull final String key) {
        String value = get(key);
        return asDouble(key, value);
    }

    @NotNull
    public Double getDouble(
            @NotNull final String key,
            @Nullable final String defaultValue
    ) {
        String value = get(key, defaultValue);
        return asDouble(key, value);
    }

    @NotNull
    public Double getDouble(
            @NotNull final String key,
            final double defaultValue
    ) {
        return getDouble(key, Double.toString(defaultValue));
    }

    @NotNull
    public Boolean getBoolean(@NotNull final String key) {
        String value = get(key);
        return Boolean.parseBoolean(value);
    }

    @NotNull
    public Boolean getBoolean(
            @NotNull final String key,
            final boolean defaultValue
    ) {
        return getBoolean(key, Boolean.toString(defaultValue));
    }

    @NotNull
    public Boolean getBoolean(
            @NotNull final String key,
            @Nullable final String defaultValue
    ) {
        return Boolean.parseBoolean(get(key, defaultValue));
    }

    @NotNull
    public String get(
            @NotNull final String key,
            @Nullable final String defaultValue
    ) {
        return dataAccess.read(() -> this.properties.getOrDefault(key, defaultValue));
    }

    @NotNull
    public String get(@NotNull final String key) {
        return dataAccess.read(() -> {
            String property = this.properties.get(Keys.format(key));

            if (property == null) {
                throw new PropertyNotFoundException(key);
            }

            return property;
        });
    }

    @NotNull
    public Collection<String> getAndSplit(@NotNull final String key) {
        String property = get(key, "");
        return Arrays.asList(property.split(","));
    }

    @NotNull
    public Collection<Boolean> getAndSplitBoolean(@NotNull final String key) {
        return getAndSplit(key).stream()
                .map(Boolean::parseBoolean)
                .collect(Collectors.toList());
    }

    @NotNull
    public Collection<Integer> getAndSplitInt(@NotNull final String key) {
        return getAndSplit(key).stream()
                .map(it -> asInt(key, it))
                .collect(Collectors.toList());
    }

    @NotNull
    public Collection<Float> getAndSplitFloat(@NotNull final String key) {
        return getAndSplit(key).stream()
                .map(it -> asFloat(key, it))
                .collect(Collectors.toList());
    }

    @NotNull
    public Collection<Double> getAndSplitDouble(@NotNull final String key) {
        return getAndSplit(key).stream()
                .map(it -> asDouble(key, it))
                .collect(Collectors.toList());
    }

    /* Parsing functions */

    private static int asInt(
            @NotNull final String key,
            @NotNull final String value
    ) {
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException numberFormatException) {
            throw new InvalidPropertyTypeException(key, value, Integer.class);
        }
    }

    private static float asFloat(
            @NotNull final String key,
            @NotNull final String value
    ) {
        try {
            return Float.parseFloat(value.replace(",", "."));
        } catch (final NumberFormatException numberFormatException) {
            throw new InvalidPropertyTypeException(key, value, Float.class);
        }
    }

    private static double asDouble(
            @NotNull final String key,
            @NotNull final String value
    ) {
        try {
            return Double.parseDouble(value.replace(",", "."));
        } catch (final NumberFormatException numberFormatException) {
            throw new InvalidPropertyTypeException(key, value, Double.class);
        }
    }

    @NotNull
    private static Properties parse(@NotNull final String s) throws IOException {
        Properties properties = new Properties();
        try (@NotNull final StringReader stringReader = new StringReader(s)) {
            properties.load(stringReader);
        }

        return properties;
    }

    @Override
    public void close() {
        clear();
    }

    public TypedProperties copy() {
        TypedProperties copiedInstance = new TypedProperties();
        copiedInstance.addAll(this);
        return copiedInstance;
    }
}
