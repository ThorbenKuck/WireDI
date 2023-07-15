package com.wiredi.processor.tck.domain.transactional;

import com.wiredi.annotations.Wire;
import com.wiredi.processor.tck.infrastructure.TckTestCase;
import org.junit.jupiter.api.DynamicNode;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@Wire
public class TransactionalTestController implements TckTestCase {

	private final TestService testService;

	public TransactionalTestController(TestService testService) {
		this.testService = testService;
	}

	@Override
	public Collection<DynamicNode> dynamicTests() {
		return List.of(
				dynamicTest("The aspect overwrites the return value", () -> assertThat(testService.execute("Test")).isEqualTo("[TRANSACTIONAL]: [TRANSACTIONAL]: Processed: Test"))
		);
	}
}
