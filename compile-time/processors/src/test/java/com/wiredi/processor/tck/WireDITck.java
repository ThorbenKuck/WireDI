package com.wiredi.processor.tck;

import com.wiredi.domain.provider.TypeIdentifier;
import com.wiredi.processor.tck.domain.InjectionTest;
import com.wiredi.processor.tck.domain.ordered.CommandBasedStringBuilder;
import com.wiredi.processor.tck.domain.override.OverwritingTestClass;
import com.wiredi.processor.tck.domain.override.OverwrittenTestClass;
import com.wiredi.processor.tck.domain.provide.CoffeeMachine;
import com.wiredi.processor.tck.infrastructure.TckTestCase;
import com.wiredi.runtime.WireRepository;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;

public class WireDITck {

	private final WireRepository wireRepository = WireRepository.open();

	@TestFactory
	public Collection<DynamicNode> verifyThatOrderingWorksCorrectly() {
		assertThat(wireRepository.tryGet(CommandBasedStringBuilder.class))
				.withFailMessage("The CommandBasedStringBuilder was not wired correctly")
				.isPresent();

		return wireRepository.get(CommandBasedStringBuilder.class).dynamicTests();
	}

	@TestFactory
	public Collection<DynamicNode> verifyThatProducersWithQualifiersWork() {
		assertThat(wireRepository.tryGet(CoffeeMachine.class))
				.withFailMessage("The Coffee Machine was not wired correctly")
				.isPresent();

		return wireRepository.get(CoffeeMachine.class).dynamicTests();
	}

	@TestFactory
	public Collection<DynamicNode> verifyThatSimpleInjectionWorks() {
		assertThat(wireRepository.tryGet(InjectionTest.class))
				.withFailMessage("The InjectionTest was not wired correctly")
				.isPresent();

		return wireRepository.get(InjectionTest.class).dynamicTests();
	}

	@TestFactory
	public Collection<DynamicNode> overwritingBehaviourTest() {
		assertThat(wireRepository.tryGet(OverwrittenTestClass.class))
				.withFailMessage("The OverwrittenTestClass was not wired correctly")
				.isPresent();

		return wireRepository.get(OverwritingTestClass.class).dynamicTests();
	}

	@TestFactory
	public Collection<? extends DynamicNode> repeatingDynamicTestCases() {
		List<TckTestCase> tckTestCases = wireRepository.getAllUnordered(TypeIdentifier.of(TckTestCase.class));
		AtomicInteger roundCounter = new AtomicInteger(0);
		return Collections.nCopies(20, tckTestCases)
				.stream()
				.map(round -> {
					int counter = roundCounter.getAndIncrement();

					return dynamicContainer("Repetition-" + counter, () -> round.stream()
							.map(testCase -> dynamicContainer(testCase.getClass().getSimpleName(), () -> testCase.dynamicTests().iterator()))
							.map(it -> (DynamicNode) it).toList().iterator()
					);
				}).toList();
	}
}
