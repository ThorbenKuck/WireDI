package com.wiredi.aspects.links;

import com.wiredi.aspects.AspectHandler;
import com.wiredi.aspects.ExecutionChainLink;
import com.wiredi.aspects.ExecutionChainParameters;
import com.wiredi.aspects.ExecutionContext;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;

public class RootMethod implements ExecutionChainLink {

	@NotNull
	private final ExecutionContext<Annotation> context;

	@NotNull
	private final AspectHandler<Annotation> rootMethod;

	public RootMethod(@NotNull AspectHandler<Annotation> rootMethod) {
		this.context = new ExecutionContext<>();
		this.rootMethod = rootMethod;
	}

	@Override
	public Object executeRaw() {
		return rootMethod.process(context);
	}

	@Override
	public ExecutionContext<Annotation> context() {
		return context;
	}

	@Override
	public <T extends Annotation> ExecutionChainLink prepend(T annotation, AspectHandler<T> handler) {
		ExecutionContext<T> prependedContext = context.prepend(annotation, this);
		return ExecutionChainElement.create(prependedContext, handler);
	}

	public ExecutionChainParameters parameters() {
		return context.parameters();
	}
}
