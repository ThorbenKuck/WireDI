package com.wiredi.test.commands;

import com.wiredi.annotations.Order;
import com.wiredi.annotations.Wire;
import jakarta.inject.Singleton;

@Wire
@Order(Order.DEFAULT + 1)
public class CommandB implements Command {
	@Override
	public void execute() {
		System.out.println("Command B");
	}
}
