package com.wiredi.compiler.logger;

import javax.tools.Diagnostic;

public enum LogLevel implements LogLevelType {

	TRACE(0, Diagnostic.Kind.NOTE),
	DEBUG(10, Diagnostic.Kind.NOTE),
	INFO(20, Diagnostic.Kind.NOTE),
	WARN(30, Diagnostic.Kind.WARNING),
	ERROR(40, Diagnostic.Kind.ERROR);

	private final int level;
	private final Diagnostic.Kind diagnosticKind;

	@Override
	public boolean isEnabled(int threshold) {
		return level >= threshold;
	}

	@Override
	public int level() {
		return level;
	}

	@Override
	public Diagnostic.Kind diagnosticKind() {
		return diagnosticKind;
	}

	LogLevel(int level, Diagnostic.Kind diagnosticKind) {
		this.level = level;
		this.diagnosticKind = diagnosticKind;
	}
}
