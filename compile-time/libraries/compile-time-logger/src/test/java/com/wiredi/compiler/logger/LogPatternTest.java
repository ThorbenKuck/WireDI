package com.wiredi.compiler.logger;

import com.wiredi.compiler.logger.pattern.CompiledLogPattern;
import com.wiredi.compiler.logger.pattern.ParsedPattern;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LogPatternTest {

	@Test
	public void test() {
		// Arrange
		String message = "This is quite a long an annoying message that should not be trimmed";
		final LogPattern logPattern = new LogPattern(ParsedPattern.parse("${key1:l15.15} [${key2:5.5}] [${key2:-5.5}] : ${message}"));
		LogPattern pattern = logPattern.newInstance()
				.context("key1", "This is key 1")
				.context("key2", "This is key 2")
				.context("message", "This is quite a long an annoying message that should not be trimmed");

		// Act
		CompiledLogPattern compile = pattern.compile();

		// Assert
		var formatted = compile.format();
		assertThat(compile.layout()).isEqualTo("%s [%s] [%s] : %s");
		assertThat(compile.arguments()).containsExactly("This is key 1  ", "This ", "This ", message);
		assertThat(formatted).isEqualTo("This is key 1   [This ] [This ] : This is quite a long an annoying message that should not be trimmed");
	}

	@Test
	public void testWithDefaultLayout() {
		// Arrange
		final LogPattern logPattern = LogPattern.DEFAULT
				.newInstance()
				.context("level", "INFO")
				.context("thread", Thread.currentThread().getName())
				.context("type", LogPatternTest.class)
				.context("annotation", "")
				.context("origin", "")
				.context("message", "A log message");

		// Act
		var message = logPattern.compile().format();

		// Assert
		assertThat(message).isEqualTo("[INFO ] [main        ] [compiler.logger.LogPatternTest] [          ] [                    ] : A log message");
	}
}
