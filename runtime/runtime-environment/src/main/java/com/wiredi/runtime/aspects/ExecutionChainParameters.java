package com.wiredi.runtime.aspects;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.wiredi.runtime.lang.Preconditions.isNotNull;

public class ExecutionChainParameters {

	private static final ThreadLocal<Map<String, Object>> content = new ThreadLocal<>();

	private Map<String, Object> getContentMap() {
		Map<String, Object> contentMap = content.get();
		if (contentMap == null) {
			throw new IllegalStateException("The ExecutionChainParameters is not initialized");
		}
		return contentMap;
	}

	public Set<String> keySet() {
		return getContentMap().keySet();
	}

	private Object getParam(String name) {
		return getContentMap().get(name);
	}

	public void put(String name, Object value) {
		getContentMap().put(name, value);
	}

	public void set(Map<String, Object> params) {
		content.set(params);
	}

	public void clear() {
		content.remove();
	}

	public <T> Optional<T> get(String name) {
		return Optional.ofNullable(getParam(name)).map(it -> (T) it);
	}

	@NotNull
	public <T> T require(String name) {
		return (T) isNotNull(getParam(name), () -> "No parameter with the name " + name + " set");
	}
}
