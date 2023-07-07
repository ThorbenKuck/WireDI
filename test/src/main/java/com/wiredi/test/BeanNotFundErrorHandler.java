package com.wiredi.test;

import com.wiredi.annotations.Wire;
import com.wiredi.domain.errors.ErrorHandler;
import com.wiredi.domain.errors.ErrorHandlingResult;
import com.wiredi.runtime.exceptions.BeanNotFoundException;
import org.jetbrains.annotations.NotNull;

@Wire
public class BeanNotFundErrorHandler implements ErrorHandler<BeanNotFoundException> {
	@Override
	public @NotNull ErrorHandlingResult<BeanNotFoundException> handle(@NotNull BeanNotFoundException error) {
		return ErrorHandlingResult.with(error)
				.havingCorrectiveAction(correctiveAction(error))
				.havingErrorMessage("Could not find a bean for type " + error.getTypeIdentifier())
				.rethrowing()
				.build();
	}

	private String correctiveAction(BeanNotFoundException error) {
		StringBuilder stringBuilder = new StringBuilder("Consider defining a bean for the type ").append(error.getTypeIdentifier());

		if (error.getQualifierType() != null) {
			stringBuilder.append(" with the qualifier ").append(error.getQualifierType());
		}

		return stringBuilder.toString();
	}
}
