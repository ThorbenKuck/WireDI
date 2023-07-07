package com.wiredi.processor;

import com.wiredi.properties.keys.Key;

public enum PropertyKeys {
	DEBUG_ENABLED("flags.debug-enabled", false),
	AOP_STRICT_ANNOTATION_TARGET("flags.strict-aop-annotation-target", true),
	WARN_REFLECTION_USAGE("flags.reflection-warnings-enabled", true),
	LOG_TO_SYSTEM_OUT("flags.log-to-system-out", true),
	PARALLEL_THREAD_COUNT("parallelization.thread-count", 10),
	EXTENSION_FILE_NAME("processor.wire-di-extension-file", "wire-di.extensions"),
	PARALLEL_EXTENSION_PROCESSING("processor.wire-di-process-extension-in-parallel", false),
	INJECT_ANNOTATIONS("processor.injections.annotations", "Inject"),
	INJECT_SUPPORT_INHERITANCE("processor.injections.support-inheritance", false);

	private final String rawKey;
	private final Object defaultValue;

	PropertyKeys(String rawKey, Object defaultValue) {
		this.rawKey = rawKey;
		this.defaultValue = defaultValue;
	}

	public Key getRawKey() {
		return Key.just(rawKey);
	}

	public Object getDefaultValue() {
		return defaultValue;
	}
}
