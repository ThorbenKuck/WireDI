package com.wiredi.compiler.logger.slf4j;

import com.wiredi.compiler.logger.LogPattern;
import com.wiredi.compiler.logger.pattern.ParsedPattern;
import com.wiredi.runtime.Environment;
import com.wiredi.runtime.properties.Key;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import javax.annotation.processing.Messager;
import java.lang.reflect.Field;

public class Slf4jHijacker {

    private final MessagerSLF4JServiceProvider provider;

    public Slf4jHijacker() {
        this.provider = new MessagerSLF4JServiceProvider();
    }

    public void hijackSlf4j() {
        try {
            // Now the ugly part, override the fields inside the LoggerFactory
            Field f = LoggerFactory.class.getDeclaredField("PROVIDER");
            f.setAccessible(true);
            f.set(null, provider);

            Field i = LoggerFactory.class.getDeclaredField("INITIALIZATION_STATE");
            i.setAccessible(true);
            i.set(null, 3);

        } catch (Exception ex) {
            throw new RuntimeException("Failed to hijack SLF4J LoggerFactory", ex);
        }

        provider.initialize();
    }

    public void initialize(Environment environment, Messager messager) {
        environment.properties()
                .subsetOf("compiler.logging.level.", Level.class)
                .forEach(provider.loggerFactory.logLevelRegistry::set);
        environment.getProperty(Key.just("compiler.logging.level"), Level.class).ifPresent(provider.loggerFactory.logLevelRegistry::setDefault);

        environment.properties()
                .subsetOf("compiler.logging.console.", Boolean.class)
                .forEach(provider.loggerFactory.logToConsoleRegistry::set);
        environment.getProperty(Key.just("compiler.logging.console"), Boolean.class).ifPresent(provider.loggerFactory.logToConsoleRegistry::setDefault);

        environment.properties()
                .subsetOf("compiler.logging.pattern.")
                .forEach(((key, logPattern) -> {
                    provider.loggerFactory.patternRegistry.set(key, new LogPattern(ParsedPattern.parse(logPattern)));
                }));
        environment.getProperty(Key.just("compiler.logging.pattern"), String.class)
                .map(ParsedPattern::parse)
                .map(LogPattern::new)
                .ifPresent(provider.loggerFactory.patternRegistry::setDefault);

        provider.loggerFactory.initialize(messager);
    }
}
