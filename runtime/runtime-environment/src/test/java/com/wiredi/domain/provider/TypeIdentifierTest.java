package com.wiredi.domain.provider;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class TypeIdentifierTest {

	@Test
	public void testThatTheSameRootTypeEqual() {
		// Arrange
		TypeIdentifier<?> first = TypeIdentifier.of(String.class);
		TypeIdentifier<?> second = TypeIdentifier.of(String.class);

		// Act
		boolean equals = first.equals(second);

		// Assert
		assertThat(equals).isTrue();
	}

	@Test
	public void testThatDifferentRootTypesDoNotEqual() {
		// Arrange
		TypeIdentifier<?> first = TypeIdentifier.of(String.class);
		TypeIdentifier<?> second = TypeIdentifier.of(Integer.class);

		// Act
		boolean equals = first.equals(second);

		// Assert
		assertThat(equals).isFalse();
	}

	@Test
	public void testThatMatchingGenericsAreEqual() {
		// Arrange
		TypeIdentifier<?> first = TypeIdentifier.of(List.class).withGeneric(String.class);
		TypeIdentifier<?> second = TypeIdentifier.of(List.class).withGeneric(String.class);

		// Act
		boolean equals = first.equals(second);

		// Assert
		assertThat(equals).isTrue();
	}

	@Test
	public void testThatNotMatchingGenericsAreNotEqual() {
		// Arrange
		TypeIdentifier<?> first = TypeIdentifier.of(List.class).withGeneric(String.class);
		TypeIdentifier<?> second = TypeIdentifier.of(List.class).withGeneric(Integer.class);

		// Act
		boolean equals = first.equals(second);

		// Assert
		assertThat(equals).isFalse();
	}

	@Test
	public void testThatALessSpecificTypeIdentifierMatchesAMoreSpecificOne() {
		// Arrange
		TypeIdentifier<?> first = TypeIdentifier.of(List.class);
		TypeIdentifier<?> second = TypeIdentifier.of(List.class).withGeneric(Integer.class);

		// Act
		boolean equals = first.equals(second);

		// Assert
		assertThat(equals).isTrue();
	}

	@Test
	public void testThatAMoreSpecificTypeIdentifierDoesNotMatchALessSpecific() {
		// Arrange
		TypeIdentifier<?> first = TypeIdentifier.of(List.class).withGeneric(Integer.class);
		TypeIdentifier<?> second = TypeIdentifier.of(List.class);

		// Act
		boolean equals = first.equals(second);

		// Assert
		assertThat(equals).isFalse();
	}

	@Test
	public void testThatAMuchLessSpecificTypeIdentifierMatchesAMuchMoreSpecificOne() {
		// Arrange
		TypeIdentifier<?> first = TypeIdentifier.of(List.class);
		TypeIdentifier<?> second = TypeIdentifier.of(List.class).withGeneric(TypeIdentifier.of(TypeIdentifier.class).withGeneric(String.class));

		// Act
		boolean equals = first.equals(second);

		// Assert
		assertThat(equals).isTrue();
	}

}