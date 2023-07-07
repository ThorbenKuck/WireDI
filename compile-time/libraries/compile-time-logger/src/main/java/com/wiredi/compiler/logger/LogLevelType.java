package com.wiredi.compiler.logger;

import javax.tools.Diagnostic;

public interface LogLevelType {
	boolean isEnabled(int threshold);

	default boolean isEnabled(LogLevelType threshold) {
		return isEnabled(threshold.level());
	}
	default boolean isDisabled(LogLevelType threshold) {
		return !isEnabled(threshold);
	}

	default boolean isDisabled(int threshold) {
		return !isEnabled(threshold);
	}

	int level();

	Diagnostic.Kind diagnosticKind();
}
