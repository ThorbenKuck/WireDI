package com.wiredi.aop;

import com.wiredi.annotations.Wire;
import com.wiredi.annotations.aspects.Aspect;
import com.wiredi.runtime.aspects.ExecutionContext;
@Wire
public class TransactionalHandler {

	@Aspect(around = Transactional.class)
	public Object handle(ExecutionContext context) {
		return context.proceed();
	}

	private String withPrefix(Object object) {
		return "[TRANSACTIONAL]: " + object;
	}
}
