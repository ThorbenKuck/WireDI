package com.wiredi.processor.tck.domain.transactional;

import com.wiredi.annotations.Wire;
import com.wiredi.runtime.aspects.AspectHandler;
import com.wiredi.processor.tck.infrastructure.TckTestCase;
import org.junit.jupiter.api.DynamicNode;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@Wire
public class TransactionalTestController implements TckTestCase {

	private final TestService testService;

	private final List<AspectHandler> aspectHandlers;

	public TransactionalTestController(TestService testService, List<AspectHandler> aspectHandlers) {
		this.testService = testService;
		this.aspectHandlers = aspectHandlers;
	}

	@Override
	public Collection<DynamicNode> dynamicTests() {
		return List.of(
				dynamicTest("The aspect overwrites the return value", () -> assertThat(testService.execute("Test")).isEqualTo("[TRANSACTIONAL]: Processed: Test")),
				dynamicTest("Overwritten methods still invoke the aspect of other annotated methods", () -> assertThat(testService.handler("Test")).isEqualTo("[TRANSACTIONAL]: [TRANSACTIONAL]: Processed: Test")),
				dynamicTest("Without nested support, The aspect overwrites the return value", () -> assertThat(testService.executeNotNested("Test")).isEqualTo("[TRANSACTIONAL]: Processed: Test")),
				dynamicTest("Without nested support, Overwritten methods still invoke the aspect of other annotated methods", () -> assertThat(testService.handlerNotNested("Test")).isEqualTo("[TRANSACTIONAL]: Processed: Test"))
		);
	}
}
