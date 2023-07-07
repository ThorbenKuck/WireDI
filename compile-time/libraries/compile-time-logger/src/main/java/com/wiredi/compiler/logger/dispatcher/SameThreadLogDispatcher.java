package com.wiredi.compiler.logger.dispatcher;

import com.wiredi.compiler.logger.LogEntry;
import com.wiredi.compiler.logger.writer.LogWriter;

import java.util.Arrays;
import java.util.List;

public class SameThreadLogDispatcher implements LogDispatcher {

	private final List<LogWriter> writerList;

	public SameThreadLogDispatcher(LogWriter... writerList) {
		this.writerList = Arrays.asList(writerList);
	}

	@Override
	public void dispatch(LogEntry logEntry) {
		writerList.forEach(logWriter -> logWriter.write(logEntry));
	}
}
