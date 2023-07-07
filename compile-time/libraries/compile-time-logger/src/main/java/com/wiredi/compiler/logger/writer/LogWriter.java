package com.wiredi.compiler.logger.writer;

import com.wiredi.compiler.logger.LogEntry;

public interface LogWriter {

	void write(LogEntry logEntry);

}
