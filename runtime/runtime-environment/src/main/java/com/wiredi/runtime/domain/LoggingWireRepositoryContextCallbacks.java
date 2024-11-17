package com.wiredi.runtime.domain;

import com.google.auto.service.AutoService;
import com.wiredi.logging.Logging;
import com.wiredi.runtime.Environment;
import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.async.StateFull;
import com.wiredi.runtime.beans.BeanContainer;
import com.wiredi.runtime.properties.Key;
import com.wiredi.runtime.time.Timed;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@AutoService(WireRepositoryContextCallbacks.class)
public class LoggingWireRepositoryContextCallbacks implements WireRepositoryContextCallbacks {

    private static final Logging logger = Logging.getInstance(WireRepository.class);
    private static final AtomicBoolean LOGGED_PROCESSORS_WARNING = new AtomicBoolean(false);
    private static final Key ENABLED_KEY = Key.just("wiredi.callbacks.logging.enabled");
    private static final Key CHECK_PROCESSOR_JAR = Key.just("wiredi.check-processors");

    private static synchronized void tryLogProcessorWarning(Environment environment) {
        if (LOGGED_PROCESSORS_WARNING.get()) {
            return;
        }
        LOGGED_PROCESSORS_WARNING.set(true);

        if (!environment.getProperty(CHECK_PROCESSOR_JAR, true)) {
            return;
        }

        try {
            Class.forName("com.wiredi.compiler.processors.WireProcessor");
            System.err.println("It appears as if you have the WireDi annotation processors in you classpath. It is recommended to have the processors only available during compilation.");
            logger.warn("It appears as if you have the WireDi annotation processors in you classpath. It is recommended to have the processors only available during compilation.");
        } catch (ClassNotFoundException ignored) {
            // This exception means that the class is not found, so no further actions are required.
            // We will not logg this or preserve this, as this is what we want to happen.
        }
    }

    @Override
    public void loadingStarted(@NotNull WireRepository wireRepository) {
        if (wireRepository.environment().getProperty(ENABLED_KEY, true)) {
            logger.debug("Starting to load the WireRepository");
        }
    }

    @Override
    public void loadedEnvironment(@NotNull Timed timed, @NotNull Environment environment) {
        if (environment.getProperty(ENABLED_KEY, true)) {
            logger.info(() -> "Environment loaded in " + timed);
            tryLogProcessorWarning(environment);
        }
    }

    @Override
    public void loadedBeanContainer(@NotNull Timed timed, @NotNull WireRepository wireRepository, @NotNull BeanContainer beanContainer) {
        if (wireRepository.environment().getProperty(ENABLED_KEY, true)) {
            logger.info(() -> "BeanContainer loaded in " + timed);
        }
    }

    @Override
    public void configuredEnvironment(@NotNull Timed timed, @NotNull Environment environment) {
        if (environment.getProperty(ENABLED_KEY, true)) {
            logger.info(() -> "Environment was configured with beans in " + timed);
        }
    }

    @Override
    public void loadedEagerClasses(@NotNull Timed timed, @NotNull WireRepository wireRepository, @NotNull List<? extends Eager> eagerInstances) {
        if (wireRepository.environment().getProperty(ENABLED_KEY, true)) {
            logger.info(() -> eagerInstances.size() + " Eager instances loaded in " + timed);
        }
    }

    @Override
    public void synchronizedOnStates(Timed timed, WireRepository wireRepository, List<StateFull> stateFulls) {
        if (wireRepository.environment().getProperty(ENABLED_KEY, true)) {
            logger.info(() -> stateFulls.size() + " States where loaded in " + timed);
        }
    }

    @Override
    public void loadingFinished(@NotNull Timed timed, @NotNull WireRepository wireRepository) {
        if (wireRepository.environment().getProperty(ENABLED_KEY, true)) {
            logger.info(() -> "WireRepository was completely loaded in " + timed);
        }
    }

    @Override
    public int getOrder() {
        return LAST;
    }
}
