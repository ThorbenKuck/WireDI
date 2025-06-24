package com.wiredi.compiler.logger;

public class LoggerProperties {

	private boolean logToSystemOut = true;
	private boolean warnReflectionUsage = true;

	public boolean logToSystemOut() {
		return logToSystemOut;
	}

	public boolean warnReflectionUsage() {
		return warnReflectionUsage;
	}

	public void logToSystemOut(boolean logToSystemOut) {
		this.logToSystemOut = logToSystemOut;
	}

	public void setWarnReflectionUsage(boolean warnReflectionUsage) {
		this.warnReflectionUsage = warnReflectionUsage;
	}
}
