package com.wiredi.aspects.links;

import com.wiredi.aspects.AspectHandler;
import com.wiredi.aspects.ExecutionChainLink;
import com.wiredi.aspects.ExecutionContext;
import com.wiredi.domain.AnnotationMetaData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;

public class ExecutionChainElement implements ExecutionChainLink {

	@NotNull
	private final ExecutionContext context;

	@NotNull
	private final AspectHandler handler;

	public ExecutionChainElement(
			@NotNull ExecutionContext context,
			@NotNull AspectHandler handler
	) {
		this.context = context;
		this.handler = handler;
	}

	@NotNull
	public static <T extends Annotation> ExecutionChainElement create(
			@NotNull ExecutionContext annotation,
			@NotNull AspectHandler function
	) {
		return new ExecutionChainElement(annotation, function);
	}

	@Override
	@NotNull
	public ExecutionContext context() {
		return context;
	}

	@Override
	@Nullable
	public Object executeRaw() {
		return handler.process(context);
	}

	@Override
	@NotNull
	public ExecutionChainLink prepend(
			@NotNull Annotation annotation,
			@NotNull AspectHandler handler
	) {
		ExecutionContext prependedContext = context.prepend(AnnotationMetaData.of(annotation), this);
		return ExecutionChainElement.create(prependedContext, handler);
	}

	@Override
	@NotNull
	public ExecutionChainLink prepend(
			@NotNull AnnotationMetaData annotation,
			@NotNull AspectHandler handler
	) {
		ExecutionContext prependedContext = context.prepend(annotation, this);
		return ExecutionChainElement.create(prependedContext, handler);
	}
}
