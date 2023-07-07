package com.wiredi.compiler.logger.writer;

import com.wiredi.compiler.logger.LogEntry;
import com.wiredi.compiler.logger.LogPattern;

import java.io.PrintStream;

public class PrintStreamLogWriter implements LogWriter {

	private final LogPattern logPattern;
	private final PrintStream printStream;

	public PrintStreamLogWriter(LogPattern logPattern, PrintStream printStream) {
		this.logPattern = logPattern;
		this.printStream = printStream;
	}

	@Override
	public void write(LogEntry logEntry) {
		String finalMessage = logPattern.newInstance()
				.context(logEntry)
				.compile()
				.format();

		printStream.println(finalMessage);
	}
}
