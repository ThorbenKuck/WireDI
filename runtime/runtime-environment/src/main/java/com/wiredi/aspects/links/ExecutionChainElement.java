package com.wiredi.aspects.links;

import com.wiredi.aspects.AspectHandler;
import com.wiredi.aspects.ExecutionChainLink;
import com.wiredi.aspects.ExecutionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;

public class ExecutionChainElement<T extends Annotation> implements ExecutionChainLink {

	@NotNull
	private final ExecutionContext<T> context;

	@NotNull
	private final AspectHandler<T> function;

	public ExecutionChainElement(
			@NotNull ExecutionContext<T> context,
			@NotNull AspectHandler<T> function
	) {
		this.context = context;
		this.function = function;
	}

	@NotNull
	public static <T extends Annotation> ExecutionChainElement<T> create(
			@NotNull ExecutionContext<T> annotation,
			@NotNull AspectHandler<T> function
	) {
		return new ExecutionChainElement<>(annotation, function);
	}

	@Override
	@NotNull
	public ExecutionContext<T> context() {
		return context;
	}

	@Override
	@Nullable
	public Object executeRaw() {
		return function.process(context);
	}

	@Override
	@NotNull
	public <S extends Annotation> ExecutionChainLink prepend(
			@NotNull S annotation,
			@NotNull AspectHandler<S> handler
	) {
		ExecutionContext<S> prependedContext = context.prepend(annotation, this);
		return ExecutionChainElement.create(prependedContext, handler);
	}
}
