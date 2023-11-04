package com.wiredi.compiler.logger;

import com.wiredi.lang.collections.TypeMap;

import java.util.function.Supplier;

public class LoggerCache {

	private final TypeMap<Logger> cache = new TypeMap<>();

	public Logger getOrSet(Class<?> type, Supplier<Logger> supplier) {
		synchronized(cache) { return cache.computeIfAbsent(type, supplier); }
	}
}
