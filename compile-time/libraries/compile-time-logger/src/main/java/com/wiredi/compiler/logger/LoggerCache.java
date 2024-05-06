package com.wiredi.compiler.logger;

import com.wiredi.runtime.collections.ConcurrentTypeMap;
import com.wiredi.runtime.collections.TypeMap;
import com.wiredi.compiler.logger.dispatcher.LogDispatcher;
import com.wiredi.compiler.logger.dispatcher.WorkerThreadLogDispatcher;
import com.wiredi.compiler.logger.writer.MessagerLogWriter;
import com.wiredi.compiler.logger.writer.SystemOutLogWriter;
import com.wiredi.runtime.lang.ThrowingSupplier;
import com.wiredi.runtime.values.Value;

import java.util.function.BiFunction;

public class LoggerCache {

	private static final TypeMap<Logger> cache = new ConcurrentTypeMap<>();
	private static LoggerProperties loggerProperties = new LoggerProperties();
	public static final ThrowingSupplier<LogDispatcher, ?> DEFAULT_DISPATCHER = () -> new WorkerThreadLogDispatcher(
			new SystemOutLogWriter(LogPattern.DEFAULT, loggerProperties),
			new MessagerLogWriter()
	);
	private static Value<LogDispatcher> logDispatcher = Value.lazy(DEFAULT_DISPATCHER);

	public static LoggerProperties getDefaultLoggerProperties() {
		return LoggerCache.loggerProperties;
	}

	public static LogDispatcher getDefaultLogDispatcher() {
		return LoggerCache.logDispatcher.get();
	}

	public static void setDefaultProperties(LoggerProperties loggerProperties) {
		LoggerCache.loggerProperties = loggerProperties;
	}

	public static void setDefaultLogDispatcher(LogDispatcher logDispatcher) {
		LoggerCache.logDispatcher.set(logDispatcher);
	}

	public static Logger getOrSet(Class<?> type, BiFunction<LoggerProperties, LogDispatcher, Logger> function) {
		synchronized(cache) { return cache.computeIfAbsent(type, () -> function.apply(loggerProperties, logDispatcher.get())); }
	}
}
