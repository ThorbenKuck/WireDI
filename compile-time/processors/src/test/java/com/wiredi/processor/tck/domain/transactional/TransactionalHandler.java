package com.wiredi.processor.tck.domain.transactional;

import com.wiredi.annotations.Wire;
import com.wiredi.annotations.aspects.Aspect;
import com.wiredi.aspects.ExecutionContext;
import com.wiredi.runtime.WireRepository;

@Wire
public class TransactionalHandler {

	@Aspect(around = Transactional.class)
	public String handle(ExecutionContext<Transactional> context, WireRepository wireRepository) {
		Object returnValue = context.proceed();
		return "[TRANSACTIONAL]: " + returnValue;
	}
}
