package com.wiredi.compiler.logger;

public class LoggerProperties {

	private boolean logToSystemOut = true;
	private LogLevelType logLevel = LogLevel.INFO;
	private boolean warnReflectionUsage = true;

	public boolean logToSystemOut() {
		return logToSystemOut;
	}

	public LogLevelType logLevel() {
		return logLevel;
	}

	public boolean warnReflectionUsage() {
		return warnReflectionUsage;
	}

	public void setLogToSystemOut(boolean logToSystemOut) {
		this.logToSystemOut = logToSystemOut;
	}

	public void setLogLevel(LogLevelType logLevel) {
		this.logLevel = logLevel;
	}

	public void setWarnReflectionUsage(boolean warnReflectionUsage) {
		this.warnReflectionUsage = warnReflectionUsage;
	}
}
