package com.github.thorbenkuck.di.processor;

public enum PropertyKeys {
	DEBUG_ENABLED("flags.debug-enabled", false),
	AOP_STRICT_ANNOTATION_TARGET("flags.strict-aop-annotation-target", true),
	WARN_REFLECTION_USAGE("flags.reflection-warnings-enabled", true),
	LOG_TO_SYSTEM_OUT("flags.log-to-system-out", true),
	PARALLEL_THREAD_COUNT("parallelization.thread-count", 10);

	private final String rawKey;
	private final Object defaultValue;

	PropertyKeys(String rawKey, Object defaultValue) {
		this.rawKey = rawKey;
		this.defaultValue = defaultValue;
	}

	public String getRawKey() {
		return rawKey;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public boolean asBoolean() {
		return ProcessorProperties.isEnabled(this);
	}

	public int asInt() {
		return ProcessorProperties.getCount(this);
	}
}
