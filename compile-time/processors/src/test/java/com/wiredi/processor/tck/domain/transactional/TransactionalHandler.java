package com.wiredi.processor.tck.domain.transactional;

import com.wiredi.annotations.Order;
import com.wiredi.annotations.Wire;
import com.wiredi.annotations.aspects.Aspect;
import com.wiredi.runtime.aspects.ExecutionContext;

@Wire
@Order(1)
public class TransactionalHandler {

	@Aspect(around = Transactional.class)
	public String handle(ExecutionContext context, TransactionState state) {
		boolean preventNested = context.findAnnotation(Transactional.class)
				.flatMap(it -> it.getBoolean("preventNested"))
				.orElse(false);

		if (preventNested) {
			if (state.isActive()) {
				return context.proceed();
			}
			state.setActive(true);
			Object returnValue = context.proceed();
			state.setActive(false);
			return withPrefix(returnValue);
		}

		return withPrefix(context.proceed());
	}

	private String withPrefix(Object object) {
		return "[TRANSACTIONAL]: " + object;
	}
}
