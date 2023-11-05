package com.wiredi.aspects.links;

import com.wiredi.aspects.AspectHandler;
import com.wiredi.aspects.ExecutionChainLink;
import com.wiredi.aspects.ExecutionChainParameters;
import com.wiredi.aspects.ExecutionContext;
import com.wiredi.domain.AnnotationMetaData;
import com.wiredi.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class RootMethod implements ExecutionChainLink {

	@NotNull
	private final ExecutionContext context = new ExecutionContext();
	@NotNull
	private final AspectHandler rootMethod;
	@NotNull
	private final String methodName;
	@NotNull
	private final Map<String, TypeIdentifier<?>> parameters;

	public RootMethod(
			@NotNull AspectHandler rootMethod,
			@NotNull String methodName,
			@NotNull Map<String, TypeIdentifier<?>> parameters
	) {
		this.rootMethod = rootMethod;
		this.methodName = methodName;
		this.parameters = parameters;
	}

	@Override
	public Object executeRaw() {
		return rootMethod.process(context);
	}

	@Override
	public ExecutionContext context() {
		return context;
	}

	@Override
	public ExecutionChainLink prepend(
			@NotNull AnnotationMetaData annotation,
			@NotNull AspectHandler handler
	) {
		ExecutionContext prependedContext = context.prepend(annotation, this);
		return ExecutionChainElement.create(prependedContext, handler);
	}

	@NotNull
	public String getMethodName() {
		return methodName;
	}

	@NotNull
	public Map<String, TypeIdentifier<?>> parameterTypes() {
		return parameters;
	}

	@NotNull
	public ExecutionChainParameters parameters() {
		return context.parameters();
	}

	public static RootMethod just(
			@NotNull String methodName,
			@NotNull AspectHandler aspectHandler
	) {
		return new Builder(methodName).build(aspectHandler);
	}

	public static Builder newInstance(String methodName) {
		return new Builder(methodName);
	}

	public static class Builder {

		@NotNull
		private final String methodName;
		private final Map<String, TypeIdentifier<?>> parameters = new HashMap<>();

		public Builder(@NotNull String methodName) {
			this.methodName = methodName;
		}

		public Builder withParameter(
				@NotNull String name,
				@NotNull TypeIdentifier<?> type
		) {
			parameters.put(name, type);
			return this;
		}

		public RootMethod build(@NotNull AspectHandler rootMethod) {
			return new RootMethod(rootMethod, methodName, parameters);
		}
	}
}
