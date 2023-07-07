package com.wiredi.processor.tck.domain.ordered.commands;

import com.wiredi.annotations.Order;
import com.wiredi.annotations.Wire;
import com.wiredi.processor.tck.domain.ordered.Command;
import com.wiredi.processor.tck.domain.ordered.CommandContext;

@Wire
@Order(1)
public class C implements Command {
	@Override
	public void modify(CommandContext context) {
		context.append("C");
	}
}
