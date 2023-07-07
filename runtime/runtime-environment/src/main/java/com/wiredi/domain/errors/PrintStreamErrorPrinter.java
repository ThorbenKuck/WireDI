package com.wiredi.domain.errors;

import java.io.PrintStream;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PrintStreamErrorPrinter implements ErrorPrinter {

	private final PrintStream printStream;

	public PrintStreamErrorPrinter(PrintStream printStream) {
		this.printStream = printStream;
	}

	@Override
	public void print(ErrorHandlingResult<?> result) {
		String start;
		if (result.unrecoverable()) {
			start = ("### Unrecoverable Exception encountered ###");
		} else {
			start = ("### Exception encountered ###");
		}

		printStream.println(start);
		String delimiter = IntStream.range(0, start.length()).boxed().map(it -> "-").
				collect(Collectors.joining(""));
		printStream.println(delimiter);

		printStream.println();
		printStream.println("Error: " + result.errorMessage());
		printStream.println();

		if (!result.correctiveAction().isEmpty()) {
			printStream.println("Corrective Action(s):");
			result.correctiveAction().forEach(action -> printStream.println(" - " + action));
		}
		printStream.println();
		printStream.println(delimiter);
	}
}
