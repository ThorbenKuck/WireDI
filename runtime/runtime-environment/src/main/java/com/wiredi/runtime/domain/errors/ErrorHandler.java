package com.wiredi.runtime.domain.errors;

import com.wiredi.runtime.domain.errors.results.ErrorHandlingResult;
import org.jetbrains.annotations.NotNull;

public interface ErrorHandler<T extends Throwable> {

	@NotNull
	ErrorHandlingResult<T> handle(@NotNull T error);

}
