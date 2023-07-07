package com.wiredi.compiler.logger.dispatcher;

import com.wiredi.compiler.logger.LogEntry;

public interface LogDispatcher {

	void dispatch(LogEntry logEntry);

}
