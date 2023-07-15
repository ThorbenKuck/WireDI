package com.wiredi.processor.tck.domain.transactional;

import com.wiredi.annotations.Wire;
import com.wiredi.aspects.AspectHandler;
import com.wiredi.aspects.ExecutionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Wire
public class CustomTransactionalAspectHandler implements AspectHandler<Transactional> {
	@Override
	public @Nullable Object process(@NotNull ExecutionContext<Transactional> context) {
		return "[TRANSACTIONAL]: " + context.proceed();
	}
}
