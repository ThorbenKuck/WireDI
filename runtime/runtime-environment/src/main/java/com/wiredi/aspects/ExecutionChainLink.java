package com.wiredi.aspects;

import com.wiredi.domain.AnnotationMetaData;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;

public interface ExecutionChainLink {

	ExecutionContext context();

	Object executeRaw();

	ExecutionChainLink prepend(AnnotationMetaData annotation, AspectHandler handler);

	default ExecutionChainLink prepend(Annotation annotation, AspectHandler handler) {
		return prepend(AnnotationMetaData.of(annotation), handler);
	}

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
