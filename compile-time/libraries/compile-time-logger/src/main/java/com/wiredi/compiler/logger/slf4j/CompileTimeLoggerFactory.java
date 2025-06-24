package com.wiredi.compiler.logger.slf4j;

import com.wiredi.compiler.logger.LogPattern;
import com.wiredi.runtime.async.DataAccess;
import org.slf4j.ILoggerFactory;
import org.slf4j.event.Level;

import javax.annotation.processing.Messager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CompileTimeLoggerFactory implements ILoggerFactory {

    private final DataAccess messagerLock = new DataAccess();
    private Messager messager;
    private final Map<String, CompileTimeLogger> cache = new ConcurrentHashMap<>();
    final LoggerRegistry<Level> logLevelRegistry = new LogLevelRegistry();
    final LogToConsoleRegistry logToConsoleRegistry = new LogToConsoleRegistry();
    final LoggerRegistry<LogPattern> patternRegistry = new LogPatternRegistry();

    private CompileTimeLoggerFactory() {}

    private static final CompileTimeLoggerFactory instance = new CompileTimeLoggerFactory();

    public static CompileTimeLoggerFactory getInstance() {
        return instance;
    }

    public static CompileTimeLogger getLogger(Class<?> type) {
        return getInstance().getLogger(type.getName());
    }

    public void initialize(Messager messager) {
        messagerLock.write(() -> {
            this.messager = messager;
            cache.values().forEach(it -> it.initialize(messager));
        });
    }

    @Override
    public CompileTimeLogger getLogger(String name) {
        return messagerLock.readValue(() -> {
            return cache.computeIfAbsent(name, n -> {
                Level level = logLevelRegistry.get(name);
                LogPattern logPattern = patternRegistry.get(name);
                boolean logToConsole = logToConsoleRegistry.get(name);
                return new CompileTimeLogger(n, level, logPattern, logToConsole, messager);
            });
        });
    }
}
