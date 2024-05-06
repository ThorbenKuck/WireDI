package com.wiredi.runtime.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class OrderedTest {

	private static final OrderedFirst first = new OrderedFirst();
	private static final OrderedSecond second = new OrderedSecond();
	private static final OrderedThird third = new OrderedThird();
	private static final OrderedLast fourth = new OrderedLast();

	private static Stream<Arguments> orderPermutations() {
		return generatePermutations(new ArrayList<>(List.of(first, second, third, fourth)))
				.stream()
				.map(Arguments::of);
	}

	private static <E> List<List<E>> generatePermutations(List<E> original) {
		if (original.isEmpty()) {
			List<List<E>> result = new ArrayList<>();
			result.add(new ArrayList<>());
			return result;
		}
		E firstElement = original.remove(0);
		List<List<E>> returnValue = new ArrayList<>();
		List<List<E>> permutations = generatePermutations(original);
		for (List<E> smallerPermutated : permutations) {
			for (int index = 0; index <= smallerPermutated.size(); index++) {
				List<E> temp = new ArrayList<>(smallerPermutated);
				temp.add(index, firstElement);
				returnValue.add(temp);
			}
		}
		return returnValue;
	}

	@ParameterizedTest
	@MethodSource("orderPermutations")
	public void testThatOrderingWorksOnInverseOrder(List<Ordered> input) {
		// Act
		List<Ordered> result = input.stream().sorted(OrderedComparator.INSTANCE).collect(Collectors.toList());

		// Assert
		List<Integer> orderValues = result.stream().map(Ordered::getOrder).collect(Collectors.toList());
		Assertions.assertThat(result).containsExactly(first, second, third, fourth);
		assertThat(orderValues).containsExactly(Integer.MIN_VALUE, Integer.MIN_VALUE + 1, 0, Integer.MAX_VALUE);
		assertThat(orderValues).containsExactly(Ordered.FIRST, Ordered.FIRST + 1, Ordered.DEFAULT, Ordered.LAST);
		assertThat(orderValues).containsExactly(Ordered.FIRST, Ordered.after(Ordered.FIRST), Ordered.DEFAULT, Ordered.LAST);
	}

	@Test
	public void testThatOrderingAnInverseListWorks() {
		// Arrange
		OrderedFirst first = new OrderedFirst();
		OrderedSecond second = new OrderedSecond();
		List<Ordered> entries = List.of(second, first);

		// Act
		List<Ordered> result = entries.stream().sorted(OrderedComparator.INSTANCE).collect(Collectors.toList());

		// Assert
		Assertions.assertThat(result).containsExactly(first, second);
	}

	@Test
	public void testThatOrderingAnAlreadyCorrectlyOrderListWorks() {
		// Arrange
		OrderedFirst first = new OrderedFirst();
		OrderedSecond second = new OrderedSecond();
		List<Ordered> entries = List.of(second, first);

		// Act
		List<Ordered> result = entries.stream().sorted(OrderedComparator.INSTANCE).collect(Collectors.toList());

		// Assert
		Assertions.assertThat(result).containsExactly(first, second);
	}

	private static class OrderedFirst implements Ordered {
		@Override
		public int getOrder() {
			return FIRST;
		}

		@Override
		public String toString() {
			return "1";
		}
	}

	private static class OrderedSecond implements Ordered {
		@Override
		public int getOrder() {
			return Ordered.after(FIRST);
		}

		@Override
		public String toString() {
			return "2";
		}
	}

	private static class OrderedThird implements Ordered {

        @Override
		public String toString() {
			return "3";
		}
	}

	private static class OrderedLast implements Ordered {
		@Override
		public int getOrder() {
			return LAST;
		}

		@Override
		public String toString() {
			return "4";
		}
	}
}
