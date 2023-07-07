package com.wiredi.domain.errors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ErrorHandlingResult<T extends Throwable> {


	@NotNull private final T cause;
	@NotNull private final String errorMessage;
	@NotNull private final List<String> correctiveActions;
	private final boolean unrecoverable;
	@Nullable private final Supplier<Throwable> rethrow;
	@NotNull private final List<ErrorPrinter> errorPrinterList = new ArrayList<>(ErrorPrinter.defaultPrinters);

	public ErrorHandlingResult(
			@NotNull T cause,
			@NotNull String errorMessage,
			@NotNull List<String> correctiveActions,
			boolean unrecoverable,
			@Nullable Supplier<Throwable> rethrow
	) {
		this.cause = cause;
		this.errorMessage = errorMessage;
		this.unrecoverable = unrecoverable;
		this.correctiveActions = correctiveActions;
		this.rethrow = rethrow;
	}

	@NotNull
	public static <T extends Throwable> Builder<T> with(@NotNull T throwable) {
		return new Builder<>(throwable);
	}

	public static <T extends Throwable> ErrorHandlingResult<T> invalid() {
		return (ErrorHandlingResult<T>) new ErrorHandlingResult<>(INVALID_THROWABLE, "INVALID", Collections.emptyList(), true, null);
	}

	public static <T extends Throwable> ErrorHandlingResult<T> unsupported() {
		return invalid();
	}

	private static final Throwable INVALID_THROWABLE = new Throwable("INVALID");

	@NotNull
	public T cause() {
		return cause;
	}

	@NotNull
	public String errorMessage() {
		return errorMessage;
	}

	public boolean unrecoverable() {
		return unrecoverable;
	}

	@NotNull
	public List<String> correctiveAction() {
		return correctiveActions;
	}

	@Nullable
	public Supplier<Throwable> rethrow() {
		return rethrow;
	}

	public void print() {
		errorPrinterList.forEach(it -> it.print(this));
	}

	public void doThrow() throws Throwable {
		if (rethrow != null) {
			throw rethrow.get();
		}
		throw cause;
	}

	public boolean valid() {
		return !this.cause.equals(INVALID_THROWABLE);
	}

	public static class Builder<T extends Throwable> {

		@NotNull
		private final T cause;

		@NotNull
		private String errorMessage;

		@NotNull
		private final List<String> correctiveAction = new ArrayList<>();

		private boolean unrecoverable = false;

		@Nullable
		private Supplier<Throwable> rethrow;

		public Builder(@NotNull T throwable) {
			this.cause = throwable;
			this.errorMessage = throwable.getMessage();
		}

		public Builder<T> havingErrorMessage(String message) {
			this.errorMessage = message;
			return this;
		}

		public Builder<T> havingCorrectiveAction(String action) {
			this.correctiveAction.add(action);
			return this;
		}

		public Builder<T> beingUnrecoverable() {
			this.unrecoverable = true;
			return this;
		}

		public Builder<T> rethrowing() {
			this.rethrow = () -> cause;
			return this;
		}

		public Builder<T> rethrowingAs(Function<T, Throwable> function) {
			this.rethrow = () -> function.apply(cause);
			return this;
		}

		public ErrorHandlingResult<T> build() {
			return new ErrorHandlingResult<>(cause, errorMessage, correctiveAction, unrecoverable, rethrow);
		}
	}
}
