package com.wiredi.properties;

import com.wiredi.lang.time.Timed;
import com.wiredi.properties.keys.Key;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class KeysTest {

	@ParameterizedTest
	@MethodSource("propertyFormattingArguments")
	public void testFormatting(String input, String expectedResult) {
		// Act
		String actualResult = Timed.of(() -> Key.format(input).value())
				.then(it -> System.out.println(it.time()))
				.value();

		// Assert
		assertThat(actualResult).isEqualTo(expectedResult);
	}

	@ParameterizedTest
	@MethodSource("propertyPrefixingArguments")
	public void testPrefixingInsideOfKeys(String prefix, String suffix, String expectedResult) {
		// Act
		Key suffixKey = Key.just(suffix);

		// Assert
		Key fullKey = suffixKey.withPrefix(prefix);

		// Assert
		assertThat(fullKey).isEqualTo(Key.just(expectedResult));
		assertThat(fullKey.value()).isEqualTo(expectedResult);
	}

	@ParameterizedTest
	@MethodSource("propertyPrefixingArguments")
	public void testRawPrefixing(String prefix, String suffix, String expectedResult) {
		// Act

		// Assert
		String fullKey = Key.joinWithSeparator(".", prefix, suffix);

		// Assert
		assertThat(fullKey).isEqualTo(expectedResult);
	}


	public static Stream<Arguments> propertyFormattingArguments() {
		return Stream.of(
				Arguments.of("my.property", "my.property"),
				Arguments.of("my.PROPERTY", "my.PROPERTY"),
				Arguments.of("my.propErty", "my.prop-erty"),
				Arguments.of("my.PROP_ERTY", "my.PROP_ERTY"),
				Arguments.of("my.prop1erty", "my.prop1erty"),
				Arguments.of("my.1PR1OPeRtY1", "my.1PR1OPe-rt-y1"),
				Arguments.of("property", "property"),
				Arguments.of("PROPERTY", "PROPERTY"),
				Arguments.of("propErty", "prop-erty"),
				Arguments.of("PROP_ERTY", "PROP_ERTY"),
				Arguments.of("prop1erty", "prop1erty"),
				Arguments.of("1PR1OPeRtY1", "1PR1OPe-rt-y1")
		);
	}

	public static Stream<Arguments> propertyPrefixingArguments() {
		return Stream.of(
				Arguments.of("my", "property", "my.property"),
				Arguments.of("my", ".property", "my.property"),
				Arguments.of("my.", ".property", "my.property"),
				Arguments.of("my.", "property", "my.property"),
				Arguments.of("", "property", "property"),
				Arguments.of("my", "", "my")
		);
	}
}