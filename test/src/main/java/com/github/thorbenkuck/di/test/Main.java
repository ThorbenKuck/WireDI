package com.github.thorbenkuck.di.test;

import com.github.thorbenkuck.di.WireConflictStrategy;
import com.github.thorbenkuck.di.WiredTypesConfiguration;
import com.github.thorbenkuck.di.WiredTypes;
import com.github.thorbenkuck.di.test.commands.Command;
import com.github.thorbenkuck.di.test.commands.CommandNote;
import com.github.thorbenkuck.di.test.inner.SuperDi;

import java.util.List;

public class Main {

	static {
		WiredTypesConfiguration.globallySetWireConflictStrategy(WireConflictStrategy.DEFAULT);
	}

	public static void main(String[] args) {
		WiredTypes wiredTypes = new WiredTypes();
		SuperDi instance = wiredTypes.getInstance(SuperDi.class);
		instance.foo();

		List<Command> commandList = wiredTypes.getAll(Command.class);
		commandList.forEach(Command::execute);

		wiredTypes.configuration().setWireConflictStrategy(WireConflictStrategy.BEST_MATCH);
		Command command = wiredTypes.getInstance(Command.class);
		command.execute();

		CommandNote instance1 = wiredTypes.getInstance(CommandNote.class);
		System.out.println(instance1);
	}
}
