package com.wiredi.properties;

import com.wiredi.properties.exceptions.PropertyNotFoundException;
import com.wiredi.properties.keys.Key;

public class PropertyReference {

	private final TypedProperties typedProperties;
	private final Key key;

	public PropertyReference(TypedProperties typedProperties, Key key) {
		this.typedProperties = typedProperties;
		this.key = key;
	}

	public Key getKey() {
		return key;
	}

	public String getValue() {
		return typedProperties.require(key);
	}

	public String getValue(String defaultValue) {
		return typedProperties.get(key, defaultValue);
	}

	public <T> T getValue(Class<T> type) {
		return typedProperties.getTyped(key, type).orElseThrow(() -> new PropertyNotFoundException(key));
	}

	public <T> T getValue(Class<T> type, T defaultValue) {
		return typedProperties.getTyped(key, type).orElse(defaultValue);
	}
}
