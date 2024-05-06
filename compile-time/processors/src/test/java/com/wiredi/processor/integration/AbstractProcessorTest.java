package com.wiredi.processor.integration;

import com.wiredi.compiler.logger.LoggerCache;
import com.wiredi.compiler.tests.junit.CompilerTest;

@CompilerTest(options = "-implicit:class")
public abstract class AbstractProcessorTest {

    static {
        LoggerCache.getDefaultLoggerProperties().logToSystemOut(false);
        System.setProperty("wire-di.generation-time", "2023-01-01T00:00Z");
    }
}
