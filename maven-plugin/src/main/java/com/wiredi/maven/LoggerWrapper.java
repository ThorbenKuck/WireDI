package com.wiredi.maven;

import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;

import java.util.function.Supplier;

public class LoggerWrapper implements Logger {

    private final Logger delegate = new ConsoleLogger(Logger.LEVEL_INFO, "WireDi");
    private static final LoggerWrapper INSTANCE = new LoggerWrapper();

    public static LoggerWrapper getInstance() {
        return INSTANCE;
    }

    public void debug(Supplier<String> message) {
        if (delegate.isDebugEnabled()) {
            delegate.debug(message.get());
        }
    }

    public void info(Supplier<String> message) {
        if (delegate.isInfoEnabled()) {
            delegate.info(message.get());
        }
    }

    public void warn(Supplier<String> message) {
        if (delegate.isWarnEnabled()) {
            delegate.warn(message.get());
        }
    }

    public void error(Supplier<String> message) {
        if (delegate.isErrorEnabled()) {
            delegate.error(message.get());
        }
    }

    public Logger unwrap() {
        return delegate;
    }

    @Override
    public void debug(String s) {
        delegate.debug(s);
    }

    @Override
    public void debug(String s, Throwable throwable) {
        delegate.debug(s, throwable);
    }

    @Override
    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    @Override
    public void info(String s) {
        delegate.info(s);
    }

    @Override
    public void info(String s, Throwable throwable) {
        delegate.info(s, throwable);
    }

    @Override
    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }

    @Override
    public void warn(String s) {
        delegate.warn(s);
    }

    @Override
    public void warn(String s, Throwable throwable) {
        delegate.warn(s, throwable);
    }

    @Override
    public boolean isWarnEnabled() {
        return delegate.isWarnEnabled();
    }

    @Override
    public void error(String s) {
        delegate.error(s);
    }

    @Override
    public void error(String s, Throwable throwable) {
        delegate.error(s, throwable);
    }

    @Override
    public boolean isErrorEnabled() {
        return delegate.isErrorEnabled();
    }

    @Override
    public void fatalError(String s) {
        delegate.fatalError(s);
    }

    @Override
    public void fatalError(String s, Throwable throwable) {
        delegate.fatalError(s, throwable);
    }

    @Override
    public boolean isFatalErrorEnabled() {
        return delegate.isFatalErrorEnabled();
    }

    @Override
    public int getThreshold() {
        return delegate.getThreshold();
    }

    @Override
    public void setThreshold(int i) {
        delegate.setThreshold(i);
    }

    @Override
    public Logger getChildLogger(String s) {
        return delegate.getChildLogger(s);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }
}
