package com.wiredi.compiler.logger;

import com.wiredi.lang.TypeMap;

import java.util.function.Supplier;

public class LoggerCache {

	private TypeMap<Object, Logger> cache = new TypeMap<>();

	public Logger getOrSet(Class<?> type, Supplier<Logger> supplier) {
		return cache.computeIfAbsent(type, supplier);
	}
}
