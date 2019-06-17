package com.github.thorbenkuck.di.resources;

import com.github.thorbenkuck.di.Configuration;
import com.github.thorbenkuck.di.SynchronizedServiceLoader;

import java.util.HashMap;
import java.util.Map;

public class ResourceRepository extends SynchronizedServiceLoader<ResourceProvider> implements Resources {

	private final Map<String, String> rawResources = new HashMap<>();

	public ResourceRepository() {
		if (Configuration.doResourceAutoLoad()) {
			load();
		}
	}

	@Override
	public Class<ResourceProvider> serviceType() {
		return ResourceProvider.class;
	}

	@Override
	public void add(ResourceProvider o) {
		set(o);
	}

	public void set(ResourceProvider resourceProvider) {
		set(resourceProvider.key(), resourceProvider.value());
	}

	public void set(String key, String value) {
		rawResources.put(key, value);
	}

	@Override
	public String getString(String id) {
		return rawResources.get(id);
	}

	@Override
	public Boolean getBoolean(String id) {
		return Boolean.parseBoolean(rawResources.get(id));
	}

	@Override
	public Short getShort(String id) {
		try {
			return Short.parseShort(rawResources.get(id));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public Integer getInteger(String id) {
		try {
			return Integer.parseInt(rawResources.get(id));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public Long getLong(String id) {
		try {
			return Long.parseLong(rawResources.get(id));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public Float getFloat(String id) {
		try {
			return Float.parseFloat(rawResources.get(id));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public Double getDouble(String id) {
		try {
			return Double.parseDouble(rawResources.get(id));
		} catch (NumberFormatException e) {
			return null;
		}
	}

}
