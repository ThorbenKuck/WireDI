package com.github.thorbenkuck.di.properties;

import com.github.thorbenkuck.di.DataAccess;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class TypedProperties implements AutoCloseable {

    private final Map<String, String> properties = new HashMap<>();
    private final DataAccess dataAccess = new DataAccess();
    private static final Map<Class<?>, PropertyConverter<?>> typeMappings = new HashMap<>();

    static {
        typeMappings.put(boolean.class, TypedProperties::getBoolean);
        typeMappings.put(int.class, TypedProperties::getInt);
        typeMappings.put(float.class, TypedProperties::getFloat);
        typeMappings.put(double.class, TypedProperties::getDouble);

        typeMappings.put(Boolean.class, TypedProperties::getBoolean);
        typeMappings.put(Integer.class, TypedProperties::getInt);
        typeMappings.put(Float.class, TypedProperties::getFloat);
        typeMappings.put(Double.class, TypedProperties::getDouble);

        typeMappings.put(String.class, TypedProperties::get);
    }

    public TypedProperties() {
    }

    public static TypedProperties fromClassPath(String path) {
        TypedProperties typedProperties = new TypedProperties();
        try {
            typedProperties.loadProperties(path);
        } catch (IOException e) {
            throw new PropertiesNotFoundException(path, e);
        }
        return typedProperties;
    }

    public static TypedProperties fromString(String content) {
        TypedProperties typedProperties = new TypedProperties();
        try {
            Properties properties = parse(content);
            typedProperties.setAll(properties);
        } catch (IOException e) {
            throw new InvalidPropertySyntaxException(content, e);
        }

        return typedProperties;
    }

    /* Modifying options */

    public void set(String key, String value) {
        dataAccess.write(() -> {
            this.properties.put(key, value);
        });
    }

    public void set(String key, boolean value) {
        set(key, Boolean.toString(value));
    }

    public void set(String key, int value) {
        set(key, Integer.toString(value));
    }

    public void set(String key, float value) {
        set(key, Float.toString(value));
    }

    public void set(String key, double value) {
        set(key, Double.toString(value));
    }

    public void setAll(Properties newProperties) {
        dataAccess.write(() -> {
            newProperties.stringPropertyNames().forEach(key -> {
                this.properties.put(key, newProperties.getProperty(key));
            });
        });
    }

    public void setAll(TypedProperties typedProperties) {
        dataAccess.write(() -> {
            properties.putAll(typedProperties.properties);
        });
    }

    public void loadProperties(String file) throws IOException {
        loadProperties(TypedProperties.class.getClassLoader().getResourceAsStream(file));
    }

    public void loadProperties(File file) throws IOException {
        loadProperties(file.toPath());
    }

    public void loadProperties(Path path) throws IOException {
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("The provided file " + path + " is not a file, but it should be one.");
        }
        loadProperties(Files.newInputStream(path));
    }

    public void loadProperties(InputStream inputStream) throws IOException {
        try {
            Properties properties = new Properties();
            properties.load(inputStream);
            setAll(properties);
            properties.clear(); // Help GC and loos value references
        } finally {
            inputStream.close();
        }
    }

    public void clear() {
        dataAccess.write(properties::clear);
    }

    /* Reading options */

    public <T> T getTyped(String key, Class<T> type) {
        return getTyped(key, type, null);
    }

    public <T> T getTyped(String key, Class<T> type, String defaultValue) {
        PropertyConverter<?> typedProvider = typeMappings.get(type);
        if (typedProvider == null) {
            throw new IllegalArgumentException("Unknown type " + type + " for the TypedProperties!");
        }

        T result = (T) typedProvider.apply(this, key, defaultValue);
        if (result == null) {
            throw new PropertyNotFoundException(key);
        }
        return result;
    }

    public Integer getInt(String key) {
        String value = get(key);
        return asInt(key, value);
    }

    public Integer getInt(String key, String defaultValue) {
        String value = get(key, defaultValue);
        return asInt(key, value);
    }

    public Integer getInt(String key, int defaultValue) {
        return getInt(key, Integer.toString(defaultValue));
    }

    public Float getFloat(String key) {
        String value = get(key);
        return asFloat(key, value);
    }

    public Float getFloat(String key, String defaultValue) {
        String value = get(key, defaultValue);
        return asFloat(key, value);
    }

    public Float getFloat(String key, float defaultValue) {
        return getFloat(key, Float.toString(defaultValue));
    }

    public Double getDouble(String key) {
        String value = get(key);
        return asDouble(key, value);
    }

    public Double getDouble(String key, String defaultValue) {
        String value = get(key, defaultValue);
        return asDouble(key, value);
    }

    public Double getDouble(String key, double defaultValue) {
        return getDouble(key, Double.toString(defaultValue));
    }

    public Boolean getBoolean(String key) {
        String value = get(key);
        return Boolean.parseBoolean(value);
    }

    public Boolean getBoolean(String key, boolean defaultValue) {
        return getBoolean(key, Boolean.toString(defaultValue));
    }

    public Boolean getBoolean(String key, String defaultValue) {
        return Boolean.parseBoolean(get(key, defaultValue));
    }

    public String get(String key, String defaultValue) {
        return dataAccess.read(() -> this.properties.getOrDefault(key, defaultValue));
    }

    public String get(String key) {
        return dataAccess.read(() -> {
            String property = this.properties.get(key);

            if (property == null) {
                throw new PropertyNotFoundException(key);
            }

            return property;
        });
    }

    public Collection<String> getAndSplit(String key) {
        String property = get(key, "");
        return Arrays.asList(property.split(","));
    }

    public Collection<Boolean> getAndSplitBoolean(String key) {
        return getAndSplit(key).stream()
                .map(Boolean::parseBoolean)
                .collect(Collectors.toList());
    }

    public Collection<Integer> getAndSplitInt(String key) {
        return getAndSplit(key).stream()
                .map(it -> asInt(key, it))
                .collect(Collectors.toList());
    }

    public Collection<Float> getAndSplitFloat(String key) {
        return getAndSplit(key).stream()
                .map(it -> asFloat(key, it))
                .collect(Collectors.toList());
    }

    public Collection<Double> getAndSplitDouble(String key) {
        return getAndSplit(key).stream()
                .map(it -> asDouble(key, it))
                .collect(Collectors.toList());
    }

    /* Parsing functions */

    private static int asInt(String key, String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException numberFormatException) {
            throw new InvalidPropertyTypeException(key, value, Integer.class);
        }
    }

    private static float asFloat(String key, String value) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException numberFormatException) {
            throw new InvalidPropertyTypeException(key, value, Integer.class);
        }
    }

    private static double asDouble(String key, String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException numberFormatException) {
            throw new InvalidPropertyTypeException(key, value, Integer.class);
        }
    }

    private static Properties parse(String s) throws IOException {
        Properties properties = new Properties();
        try (StringReader stringReader = new StringReader(s)) {
            properties.load(stringReader);
        }

        return properties;
    }

    @Override
    public void close() {
        clear();
    }
}
