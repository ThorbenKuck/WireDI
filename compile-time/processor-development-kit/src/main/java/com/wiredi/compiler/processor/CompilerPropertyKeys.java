package com.wiredi.compiler.processor;

import com.wiredi.runtime.properties.Key;

import java.io.Serializable;
import java.util.List;

public enum CompilerPropertyKeys {
    DEBUG_ENABLED("flags.debug-enabled", false),
    AOP_REQUIRES_ASPECT_TARGET_ANNOTATION("flags.aop-requires-annotation-target", true),
    AOP_STRICT_ANNOTATION_TARGET("flags.strict-aop-annotation-target", true),
    AOP_ASPECT_TARGETS("aspects.target-annotations", ""),
    WARN_REFLECTION_USAGE("flags.reflection-warnings-enabled", true),
    LOG_TO_SYSTEM_OUT("flags.log-to-system-out", true),
    PARALLEL_THREAD_COUNT("parallelization.thread-count", 10),
    EXTENSION_FILE_NAME("processor.wire-di-extension-file", "wire-di.extensions"),
    PARALLEL_EXTENSION_PROCESSING("processor.wire-di-process-extension-in-parallel", false),
    INJECT_ANNOTATIONS("processor.injections.annotations", "Inject"),
    ENABLE_GENERATED_SOURCES("processor.enable-generated-sources", true),

    /**
     * Determines if additional wire types will contain inherited super types.
     * <p>
     * If true, ths {@link TypeExtractor} will return all interfaces and superclasses of an annotated class
     * as additional wire types, as well as all interfaces and superclasses of all interfaces and superclasses.
     * <p>
     * If false, only directly declared supertypes will be returned as additional wire types.
     */
    ADDITIONAL_WIRE_TYPES_SUPPORT_INHERITANCE("processor.additional-wire-types.support-inheritance", false),

    /**
     * A list of fully qualified class names, that should not be added to the additional wire types.
     */
    ADDITIONAL_WIRE_TYPES_IGNORE("processor.additional-wire-types.ignore", List.of(
            Object.class.getName(),
            Record.class.getName(),
            Serializable.class.getName()
    ));

    private final Key rawKey;
    private final Object defaultValue;

    CompilerPropertyKeys(String rawKey, Object defaultValue) {
        this.rawKey = Key.just(rawKey);
        this.defaultValue = defaultValue;
    }

    public Key getRawKey() {
        return rawKey;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
