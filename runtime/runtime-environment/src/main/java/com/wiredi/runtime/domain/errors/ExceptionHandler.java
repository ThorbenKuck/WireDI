package com.wiredi.runtime.domain.errors;

import com.wiredi.runtime.domain.errors.results.ExceptionHandlingResult;
import org.jetbrains.annotations.NotNull;

public interface ExceptionHandler<T extends Throwable> {

	@NotNull
	ExceptionHandlingResult<T> handle(@NotNull T error);

}
