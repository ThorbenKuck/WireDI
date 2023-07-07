package com.wiredi.domain.errors;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ErrorHandler<T extends Throwable> {

	@NotNull
	ErrorHandlingResult<T> handle(@NotNull T error);

}
