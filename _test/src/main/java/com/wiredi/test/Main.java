package com.wiredi.test;

import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.WiredApplication;
import com.wiredi.runtime.WiredApplicationInstance;
import com.wiredi.test.commands.Command;
import com.wiredi.test.commands.CommandNote;
import com.wiredi.test.inner.SuperDi;
import com.wiredi.test.properties.NestedProperties;

import java.util.List;

public class Main {

	public static void main(String[] args) {
		WiredApplicationInstance applicationInstance = WiredApplication.start(container -> {
			container.announce(new Dependency());
		});
		WireContainer wireContainer = applicationInstance.wireContainer();

		wireContainer.get(MetaAnnotationTest.class);
		SuperDi instance = wireContainer.get(SuperDi.class);

		instance.foo();

		List<Command> commandList = wireContainer.getAll(Command.class);
		commandList.forEach(Command::execute);

		Command command = wireContainer.get(Command.class);
		command.execute();

		CommandNote instance1 = wireContainer.get(CommandNote.class);
		System.out.println(instance1);

		NestedProperties properties = wireContainer.get(NestedProperties.class);
		System.out.println(properties.getThird());
	}
}
