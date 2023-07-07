package com.wiredi.processor.tck.infrastructure;

import org.junit.jupiter.api.DynamicNode;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class TckCondition {

	private final String name;
	private final StackTraceElement creator;
	private final List<Error> errors = new ArrayList<>();
	private State state = State.UNDEFINED;

	private TckCondition(String name, StackTraceElement creator) {
		this.name = name;
		this.creator = creator;
	}

	public static TckCondition shouldNotFail(String name) {
		TckCondition tckCondition = new TckCondition(name, Thread.currentThread().getStackTrace()[2]);
		tckCondition.success();
		return tckCondition;
	}

	public static TckCondition mustSucceed(String name) {
		return new TckCondition(name, Thread.currentThread().getStackTrace()[2]);
	}

	public void success() {
		if (state == State.UNDEFINED) {
			this.state = State.SUCCESS;
		}
	}

	public void failure(String error) {
		this.state = State.FAILURE;
		StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
		errors.add(new Error(error, stackTraceElement));
	}

	public void failure() {
		this.state = State.FAILURE;
	}

	public void assertSuccess() {
		StringBuilder messageBuilder = new StringBuilder(System.lineSeparator()).append(name);

		if (state == State.UNDEFINED) {
			fail(messageBuilder.append(System.lineSeparator()).append(" - Condition was not invoked").toString());
		}

		if (state == State.FAILURE) {
			if (errors.isEmpty()) {
				errors.add(new Error("Condition was not succeeded", creator));
			}
			errors.forEach(error -> messageBuilder.append(System.lineSeparator()).append(" - ").append(error));
			fail(messageBuilder.toString());
		}
	}

//	public DynamicTest toDynamicTest() {
//		return dynamicTest(name, this::assertSuccess);
//	}

	public DynamicNode toDynamicTest() {
		if (errors.isEmpty()) {
			return dynamicTest(name, toUri(creator), this::assertSuccess);
		} else {
			return dynamicContainer(name, toUri(creator), errors.stream()
					.map(error -> dynamicTest(error.message, toUri(error.producer), () -> fail(error.message)))
			);
		}
	}

	public boolean isSuccessful() {
		return state == State.SUCCESS;
	}

	public boolean isFailed() {
		return state == State.FAILURE;
	}

	public boolean isUndefined() {
		return state == State.UNDEFINED;
	}

	private URI toUri(StackTraceElement stackTraceElement) {
		String line = "?line=" + stackTraceElement.getLineNumber();
		System.out.println(stackTraceElement.getFileName() + line + " => " + stackTraceElement.getMethodName());
		if (stackTraceElement.getMethodName().contains("<init>")) {
			return URI.create("class:" + stackTraceElement.getClassName() + line);
		} else {
			return URI.create("method:" + stackTraceElement.getClassName() + "#" + stackTraceElement.getMethodName());
		}
	}

	public enum State {
		UNDEFINED,
		SUCCESS,
		FAILURE
	}

	private class Error {
		private final String message;
		private final StackTraceElement producer;

		private Error(String message, StackTraceElement producer) {
			this.message = message;
			this.producer = producer;
		}
	}
}
