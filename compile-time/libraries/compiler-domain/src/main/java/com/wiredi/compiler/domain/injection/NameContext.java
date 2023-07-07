package com.wiredi.compiler.domain.injection;

import com.wiredi.lang.SafeReference;

import java.util.*;

public class NameContext {

	private final Map<String, SafeReference<Integer>> counter = new HashMap<>();
	private final Map<String, List<String>> names = new HashMap<>();

	public String nextName(String nameContext) {
		String variableName = nameContext + getCounter(nameContext).getAndUpdate(it -> it + 1);
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
