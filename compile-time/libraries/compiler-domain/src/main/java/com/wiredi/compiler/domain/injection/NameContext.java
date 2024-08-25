package com.wiredi.compiler.domain.injection;

import com.wiredi.runtime.values.SafeReference;

import java.util.*;

public class NameContext {

	private final Map<String, SafeReference<Integer>> counter = new HashMap<>();
	private final Map<String, List<String>> names = new HashMap<>();
	private final boolean ignoreFirstZeros;

    public NameContext() {
		this(true);
    }

    public NameContext(boolean ignoreFirstZeros) {
        this.ignoreFirstZeros = ignoreFirstZeros;
    }

    public String nextName(String nameContext) {
		Integer counter = getCounter(nameContext).getAndUpdate(it -> it + 1);
		String variableName;
		if (counter == 0 && ignoreFirstZeros) {
			variableName = nameContext;
		} else {
			variableName = nameContext + counter;
		}
		names.computeIfAbsent(nameContext, (c) -> new ArrayList<>()).add(variableName);
		return variableName;
	}

	public List<String> drainNamesOf(String nameContext) {
		List<String> result = names.getOrDefault(nameContext, Collections.emptyList());
		names.remove(nameContext);
		return result;
	}

	private SafeReference<Integer> getCounter(String context) {
		return counter.computeIfAbsent(context, (c) -> new SafeReference<>(0));
	}

	public void resetNameCache(String nameContext) {
		names.remove(nameContext);
	}
}
