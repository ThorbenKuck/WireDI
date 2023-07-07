package com.wiredi.domain.errors;

import java.util.List;

public interface ErrorPrinter {

	List<ErrorPrinter> defaultPrinters = List.of(new PrintStreamErrorPrinter(System.out));

	void print(ErrorHandlingResult<?> result);

}
