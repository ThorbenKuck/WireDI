package com.wiredi.aspects;

import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;

public interface ExecutionChainLink {

	ExecutionContext<? extends Annotation> context();

	Object executeRaw();

	<T extends Annotation> ExecutionChainLink prepend(T annotation, AspectHandler<T> handler);

	@Nullable
	default <S> S execute() {
		Object o = executeRaw();
		if (o == null) {
			return null;
		}
		return (S) o;
	}

	@Nullable
	default <S> S execute(Class<S> type) {
		Object o = executeRaw();
		if (o == null) {
			return null;
		}
		return type.cast(o);
	}
}
