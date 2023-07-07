package com.wiredi.processor.tck.domain.ordered;

import com.wiredi.annotations.Wire;
import com.wiredi.processor.tck.infrastructure.TckTestCase;
import org.junit.jupiter.api.DynamicNode;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@Wire
public class CommandBasedStringBuilder implements TckTestCase {

	private final List<Command> commandList;

	public CommandBasedStringBuilder(List<Command> commandList) {
		this.commandList = commandList;
	}

	public String build() {
		CommandContext context = new CommandContext();
		commandList.forEach(command -> command.modify(context));
		return context.stringify();
	}

	@Override
	public Collection<DynamicNode> dynamicTests() {
		return List.of(
				dynamicTest("Assert that 3 commands are wired", () -> assertThat(commandList).hasSize(3)),
				dynamicTest("Assert that the commands are in a correct order", () -> assertThat(build()).isEqualTo("CAB"))
		);
	}
}
