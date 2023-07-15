package com.wiredi.aspects;

import com.wiredi.aspects.links.RootMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

public class ExecutionChain {

	@NotNull
	private final RootMethod rootMethod;

	@NotNull
	private ExecutionChainLink head;

	public ExecutionChain(
			@NotNull RootMethod rootFunction
	) {
		this.rootMethod = rootFunction;
		this.head = rootFunction;
	}

	@NotNull
	public static Builder newInstance(@NotNull AspectHandler<Annotation> rootMethod) {
		return new Builder(new RootMethod(rootMethod));
	}

	@NotNull
	public <T extends Annotation> ExecutionChain prepend(@NotNull T annotation, @NotNull AspectHandler<T> function) {
		this.head = head.prepend(annotation, function);
		return this;
	}

	@NotNull
	public RootMethod rootMethod() {
		return rootMethod;
	}

	@NotNull
	public ExecutionChainLink tail() {
		return rootMethod;
	}

	@NotNull
	public ExecutionChainLink head() {
		return head;
	}

	@Nullable
	public <S> S execute(@NotNull Map<String, Object> parameters) {
		Object result = doExecute(parameters);
		if (result == null) {
			return null;
		} else {
			return (S) result;
		}
	}

	@Nullable
	public <S> S execute(@NotNull Map<String, Object> parameters, @NotNull Class<S> type) {
		Object result = doExecute(parameters);
		if (result == null) {
			return null;
		} else {
			return type.cast(result);
		}
	}

	@NotNull
	public ExecutionStage execute() {
		return new ExecutionStage();
	}

	@Nullable
	private Object doExecute(@NotNull Map<String, Object> parameters) {
		try {
			rootMethod.parameters().set(parameters);
			return head.executeRaw();
		} finally {
			rootMethod.parameters().clear();
		}
	}

	public static class Builder {

		@NotNull
		private final RootMethod rootMethod;

		@NotNull
		private final Queue<ComponentBuilder<? extends Annotation>> prepends = new LinkedBlockingDeque<>();

		public Builder(@NotNull RootMethod rootMethod) {
			this.rootMethod = rootMethod;
		}

		@NotNull
		public <S extends Annotation> Builder withProcessor(@NotNull S annotation, @NotNull AspectHandler<S> handler) {
			prepends.add(new ComponentBuilder<>(annotation, handler));
			return this;
		}

		@NotNull
		public <S extends Annotation> Builder withProcessors(@NotNull S annotation, @NotNull List<AspectHandler<S>> handlers) {
			handlers.forEach(handler -> prepends.add(new ComponentBuilder<>(annotation, handler)));
			return this;
		}

		@NotNull
		public ExecutionChain build() {
			ExecutionChain executionChain = new ExecutionChain(rootMethod);

			while (prepends.peek() != null) {
				prepend(executionChain, prepends.poll());
			}

			return executionChain;
		}

		private <T extends Annotation> void prepend(@NotNull ExecutionChain executionChain, @NotNull ComponentBuilder<T> componentBuilder) {
			executionChain.prepend(componentBuilder.annotation, componentBuilder.function);
		}

		private record ComponentBuilder<T extends Annotation>(@NotNull T annotation, @NotNull AspectHandler<T> function) {}
	}

	public class ExecutionStage {
		@NotNull
		private final Map<String, Object> content = new HashMap<>();

		@NotNull
		public ExecutionStage withParameter(@NotNull String name, @Nullable Object value) {
			content.put(name, value);
			return this;
		}

		@Nullable
		public <S> S andReturn() {
			return ExecutionChain.this.execute(content);
		}
	}
}
