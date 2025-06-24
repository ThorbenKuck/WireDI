package com.wiredi.runtime.domain.aop;

import org.jetbrains.annotations.Nullable;

/**
 * This interface is a marker interface to identify generated Proxy classes.
 * <p>
 * It has no special methods and is only used, so that at runtime a user might evaluate, if any specific class
 * is a generated proxy or not. If any class is instance of this interface, it is by contract a generated class.
 * Especially the default instance processor automatically applies this interface.
 * <p>
 * For convenience the method {@link #isProxy(Object)} may be used, to check if any specific object is a proxy. Just
 * note that you should normally never require this check. If you find yourself requiring this method, consider
 * overthinking the approach you are currently implementing.
 */
public interface AspectAwareProxy {

	static boolean isProxy(@Nullable final Object instance) {
		return instance instanceof AspectAwareProxy;
	}

	static Class<?> classOf(Object instance) {
		Class<?> type = instance.getClass();
		if (isProxy(instance)) {
			return type.getSuperclass();
		} else {
			return type;
		}
	}

	default Class<?> getProxiedClass() {
		return classOf(this);
	}
}
