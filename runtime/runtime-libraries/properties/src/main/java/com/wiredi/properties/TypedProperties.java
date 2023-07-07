package com.wiredi.properties;

import com.wiredi.lang.async.DataAccess;
import com.wiredi.properties.exceptions.PropertyNotFoundException;
import com.wiredi.properties.keys.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TypedProperties implements AutoCloseable {

	@NotNull
	private final Map<Key, String> properties = new HashMap<>();
	@NotNull
	private final DataAccess dataAccess = new DataAccess();
	private boolean respectEnvironment = true;

	@NotNull
	public static TypedProperties from(@NotNull Map<Key, String> rawProperties) {
		TypedProperties result = new TypedProperties();
		result.setAll(rawProperties);
		return result;
	}

	@NotNull
	public static TypedProperties from(@NotNull TypedProperties typedProperties) {
		TypedProperties result = new TypedProperties();
		result.setAll(typedProperties.properties);
		return result;
	}

	/* Modifying options */

	private static Optional<String> takeFromEnvironment(@NotNull final Key key) {
		String formattedKey = key.value();
		return Optional.ofNullable(System.getenv(formattedKey))
				.or(() -> Optional.ofNullable(System.getProperty(formattedKey)));
	}

	public void respectEnvironment(boolean value) {
		this.respectEnvironment = value;
	}

	public void setAll(@NotNull final Map<Key, String> rawProperties) {
		dataAccess.write(() -> properties.putAll(rawProperties));
	}

	public void setAll(@NotNull final TypedProperties typedProperties) {
		dataAccess.write(() -> properties.putAll(typedProperties.properties));
	}

	public void set(
			@NotNull final Key key,
			@NotNull final String value
	) {
		dataAccess.write(() -> properties.put(key, value));
	}

	public void set(
			@NotNull final Key key,
			final boolean value
	) {
		set(key, Boolean.toString(value));
	}

	public void set(
			@NotNull final Key key,
			final int value
	) {
		set(key, Integer.toString(value));
	}

	public void set(
			@NotNull final Key key,
			final float value
	) {
		set(key, Float.toString(value));
	}

	public void set(
			@NotNull final Key key,
			final double value
	) {
		set(key, Double.toString(value));
	}

	public void clear() {
		dataAccess.write(properties::clear);
	}

	/* Reading options */

	public boolean contains(Key key) {
		return dataAccess.readValue(() -> properties.containsKey(key));
	}

	/**
	 * The primary reading info for reading a property.
	 * <p>
	 * The access will use a read lock.
	 *
	 * @param key the key to look up
	 * @return the stored value as an optional
	 */
	private Optional<String> doGet(@NotNull final Key key) {
		if (respectEnvironment) {
			return takeFromEnvironment(key)
					.or(() -> dataAccess.readValue(() -> Optional.ofNullable(this.properties.get(key))));
		} else {
			return dataAccess.readValue(() -> Optional.ofNullable(this.properties.get(key)));
		}
	}

	@NotNull
	public <T> Optional<T> getTyped(
			@NotNull final Key key,
			@NotNull final Class<T> type
	) {
		return get(key).map(it -> TypeMapper.convert(type, key, it));
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
	public Boolean getBoolean(
			@NotNull final Key key,
			final boolean defaultValue
	) {
		return getTyped(key, boolean.class, Boolean.toString(defaultValue));
	}

	@Nullable
	public String get(
			@NotNull final Key key,
			@Nullable final String defaultValue
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
				.map(it -> ((List<String>) Arrays.asList(it.split(","))))
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

	public PropertyReference getReferenceFor(Key key) {
		return new PropertyReference(this, key);
	}

	private Supplier<PropertyNotFoundException> notFound(Key key) {
		return () -> new PropertyNotFoundException(key.value());
	}
}
