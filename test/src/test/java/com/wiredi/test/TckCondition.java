package com.wiredi.test;

import org.junit.jupiter.api.DynamicTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class TckCondition {

	private final String name;
	private final List<String> errors = new ArrayList<>();
	private State state = State.UNDEFINED;

	public TckCondition(String name) {
		this.name = name;
	}

	public static TckCondition shouldNotFail(String name) {
		TckCondition tckCondition = new TckCondition(name);
		tckCondition.success();
		return tckCondition;
	}

	public static TckCondition mustSucceed(String name) {
		return new TckCondition(name);
	}

	public void success() {
		if (state == State.UNDEFINED) {
			this.state = State.SUCCESS;
		}
	}

	public void failure(String error) {
		this.state = State.FAILURE;
		errors.add(error);
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
				errors.add("Condition was not succeeded");
			}
			errors.forEach(error -> messageBuilder.append(System.lineSeparator()).append(" - ").append(error));
			fail(messageBuilder.toString());
		}
	}

	public DynamicTest toDynamicTest() {
		return dynamicTest(name, this::assertSuccess);
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

	public enum State {
		UNDEFINED,
		SUCCESS,
		FAILURE
	}
}
