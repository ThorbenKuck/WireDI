package com.wiredi.aspects;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class ExecutionChainTest {

	@Test
	public void test() {
		// Arrange
		ExampleClass exampleClass = new ExampleClass();
		ExampleAnnotation annotation = exampleClass.getClass().getAnnotation(ExampleAnnotation.class);

		// Act
		String result = ExecutionChain.newInstance(c -> c.requireParameter("param"))
				.withProcessor(annotation, context -> context.proceed() + " From")
				.withProcessor(annotation, context -> context.proceed() + " Aspects")
				.build()
				.execute(Map.of("param", "Hello World"), String.class);

		// Assert
		assertThat(result).isEqualTo("Hello World From Aspects");
	}

	@TestFactory
	public Stream<DynamicTest> testThatAnExecutionChainCanBeReused() {
		int rounds = 10;
		ExampleClass exampleClass = new ExampleClass();
		ExampleAnnotation annotation = exampleClass.getClass().getAnnotation(ExampleAnnotation.class);

		ExecutionChain executionChain = ExecutionChain.newInstance(c -> c.requireParameter("param").toString())
				.withProcessor(annotation, context -> context.proceed() + " From")
				.withProcessor(annotation, context -> context.proceed() + " Aspects")
				.build();

		return IntStream.range(0, rounds).mapToObj(i -> {
			String expected = i + " From Aspects";
			return dynamicTest("Run " + i + " => " + expected,  () -> {
						// Act
						String result = executionChain.execute(Map.of("param", i));

						// Assert
						assertThat(result).isEqualTo(expected);
					});
				}
		);
	}

	@Test
	public void testPrependingList() {
		// Arrange
		ExampleClass exampleClass = new ExampleClass();
		ExampleAnnotation annotation = exampleClass.getClass().getAnnotation(ExampleAnnotation.class);

		// Act
		String result = ExecutionChain.newInstance(c -> c.requireParameter("param"))
				.withProcessor(annotation, context -> context.proceed() + " From")
				.withProcessor(annotation, context -> context.proceed() + " Aspects")
				.build()
				.execute()
				.withParameter("param", "Hello World")
				.andReturn();

		// Assert
		assertThat(result).isEqualTo("Hello World From Aspects");
	}

	@Retention(RetentionPolicy.RUNTIME)
	@interface ExampleAnnotation {
	}

	@ExampleAnnotation
	static class ExampleClass {
	}
}