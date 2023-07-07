package com.wiredi.compiler.logger.writer;

import com.wiredi.compiler.logger.LogPattern;

public class SystemOutLogWriter extends PrintStreamLogWriter {
	public SystemOutLogWriter(LogPattern logPattern) {
		super(logPattern, System.out);
	}
}
