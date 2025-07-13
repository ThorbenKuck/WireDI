package com.wiredi.test;

import com.wiredi.runtime.WireContainer;
import com.wiredi.test.commands.Command;
import com.wiredi.test.commands.CommandNote;
import com.wiredi.test.inner.SuperDi;
import com.wiredi.test.properties.NestedProperties;

import java.util.List;

public class Main {

	public static void main(String[] args) {
		WireContainer wireRepository = WireContainer.open();
		wireRepository.announce(new Dependency());

		wireRepository.get(MetaAnnotationTest.class);
		SuperDi instance = wireRepository.get(SuperDi.class);

		instance.foo();

		List<Command> commandList = wireRepository.getAll(Command.class);
		commandList.forEach(Command::execute);

		Command command = wireRepository.get(Command.class);
		command.execute();

		CommandNote instance1 = wireRepository.get(CommandNote.class);
		System.out.println(instance1);

		NestedProperties properties = wireRepository.get(NestedProperties.class);
		System.out.println(properties.getThird());
	}
}
