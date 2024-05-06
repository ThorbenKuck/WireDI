package com.wiredi.compiler.logger.writer;

import com.wiredi.compiler.logger.LogEntry;
import com.wiredi.compiler.logger.LogPattern;
import com.wiredi.compiler.logger.LoggerProperties;

public class SystemOutLogWriter extends PrintStreamLogWriter {
	private final LoggerProperties loggerProperties;

	public SystemOutLogWriter(LogPattern logPattern) {
		this(logPattern, new LoggerProperties());
	}


	public SystemOutLogWriter(LogPattern logPattern, LoggerProperties loggerProperties) {
		super(logPattern, System.out);
		this.loggerProperties = loggerProperties;
	}

	@Override
	public void write(LogEntry logEntry) {
		if (loggerProperties.logToSystemOut()) {
			super.write(logEntry);
		}
	}
}
