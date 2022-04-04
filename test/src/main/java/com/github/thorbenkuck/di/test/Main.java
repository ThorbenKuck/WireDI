package com.github.thorbenkuck.di.test;

import com.github.thorbenkuck.di.WireConflictStrategy;
import com.github.thorbenkuck.di.WireRepository;
import com.github.thorbenkuck.di.WiredTypesConfiguration;
import com.github.thorbenkuck.di.test.commands.Command;
import com.github.thorbenkuck.di.test.commands.CommandNote;
import com.github.thorbenkuck.di.test.inner.SuperDi;

import java.util.List;

public class Main {

	static {
		WiredTypesConfiguration.globallySetWireConflictStrategy(WireConflictStrategy.DEFAULT);
	}

	public static void main(String[] args) {
		WireRepository wireRepository = WireRepository.open();
		wireRepository.announce(new Dependency());

		SuperDi instance = wireRepository.get(SuperDi.class);

		instance.print("Bla");
		instance.printAndReturn("Blub");
		instance.foo();

		List<Command> commandList = wireRepository.getAll(Command.class);
		commandList.forEach(Command::execute);

		wireRepository.configuration().setWireConflictStrategy(WireConflictStrategy.BEST_MATCH);
		Command command = wireRepository.get(Command.class);
		command.execute();

		CommandNote instance1 = wireRepository.get(CommandNote.class);
		System.out.println(instance1);
	}
}
