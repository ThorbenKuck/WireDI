package com.wiredi.test;

import com.wiredi.runtime.WireRepository;
import com.wiredi.test.commands.Command;
import com.wiredi.test.commands.CommandNote;
import com.wiredi.test.inner.SuperDi;

import java.util.List;

public class Main {

	static {
//		WiredTypesConfiguration.globallySetWireConflictStrategy(WireConflictStrategy.DEFAULT);
	}

	public static void main(String[] args) {
		WireRepository wireRepository = WireRepository.open();
		wireRepository.announce(new Dependency());

		wireRepository.get(MetaAnnotationTest.class);
		SuperDi instance = wireRepository.get(SuperDi.class);

		instance.print("Bla");
		instance.printAndReturn("Blub");
		instance.foo();

		List<Command> commandList = wireRepository.getAll(Command.class);
		commandList.forEach(Command::execute);

//		wireRepository.properties().setWireConflictStrategy(WireConflictStrategy.BEST_MATCH);
		Command command = wireRepository.get(Command.class);
		command.execute();

		CommandNote instance1 = wireRepository.get(CommandNote.class);
		System.out.println(instance1);
	}
}
