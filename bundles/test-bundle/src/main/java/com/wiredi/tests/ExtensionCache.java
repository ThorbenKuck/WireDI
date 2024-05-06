package com.wiredi.tests;

import com.wiredi.runtime.WireRepository;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.HashMap;
import java.util.Map;

public class ExtensionCache {

	private static final Map<ExtensionContext, WireRepository> cache = new HashMap<>();

	public static WireRepository getOrCreate(ExtensionContext extensionContext) {
		return cache.computeIfAbsent(extensionContext.getRoot(), c -> WireRepository.open());
	}
}
