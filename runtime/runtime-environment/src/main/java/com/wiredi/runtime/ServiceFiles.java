package com.wiredi.runtime;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.values.Value;

import java.util.*;

public class ServiceFiles<T> {

    private final Logging logger = Logging.getInstance(ServiceFiles.class);

    private static ProviderFactory PROVIDER_FACTORY = new ServiceLoaderProviderFactory();
    private final Class<T> type;
    private final Value<Collection<ServiceLoader.Provider<T>>> providers;
    private final List<Class<? extends T>> serviceTypes;
    private final List<T> services = new ArrayList<>();
    private boolean initialized = false;
    private boolean ignoreClassNotFound = false;

    public static void setProviderFactory(ProviderFactory factory) {
        PROVIDER_FACTORY = factory;
    }

    public interface ProviderFactory {
        <T> Collection<ServiceLoader.Provider<T>> getProviders(Class<T> type);
    }

    public static class ServiceLoaderProviderFactory implements ProviderFactory {

        @Override
        public <T> Collection<ServiceLoader.Provider<T>> getProviders(Class<T> type) {
            return ServiceLoader.load(type, Thread.currentThread().getContextClassLoader()).stream().toList();
        }
    }

    public ServiceFiles(Class<T> type) {
        this.type = type;
        this.providers = Value.async(() -> PROVIDER_FACTORY.getProviders(type));
        this.serviceTypes = new ArrayList<>();
    }

    public static <T> ServiceFiles<T> getInstance(Class<T> clazz) {
        return new ServiceFiles<>(clazz);
    }

    public List<Class<? extends T>> serviceTypes() {
        return serviceTypes;
    }

    public Class<T> type() {
        return type;
    }

    public List<T> instances() {
        if (!initialized) {
            initialize();
        }

        return services;
    }

    public ServiceFiles<T> initialize(OnDemandInjector onDemandInjector) {
        if (initialized) {
            throw new IllegalStateException("ServiceFiles already initialized");
        }

        initializeServiceTypes();
        serviceTypes.stream().map(onDemandInjector::get).forEach(services::add);
        initialized = true;
        return this;
    }

    public void initialize() {
        if (initialized) {
            throw new IllegalStateException("ServiceFiles already initialized");
        }

        for (ServiceLoader.Provider<T> provider : providers.get()) {
            try {
                services.add(provider.get());
            } catch (Throwable throwable) {
                if (ignoreClassNotFound && isClassNotFoundError(throwable)) {
                    logger.warn(() -> "The " + provider.type() + " provider could not be loaded, as it caused: " + throwable.getMessage(), throwable);
                } else {
                    throw throwable;
                }
            }
        }
        initialized = true;
    }

    private void initializeServiceTypes() {
        for (ServiceLoader.Provider<T> provider : providers.get()) {
            try {
                serviceTypes.add(provider.type());
            } catch (Throwable throwable) {
                if (ignoreClassNotFound && isClassNotFoundError(throwable)) {
                    logger.warn(() -> "The " + provider.type() + " provider could not be loaded, as it caused: " + throwable.getMessage(), throwable);
                } else {
                    throw throwable;
                }
            }
        }
    }

    private boolean isClassNotFoundError(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof ClassNotFoundException ||
                    current instanceof NoClassDefFoundError) {
                return true;
            }
            // ServiceConfigurationError with specific messages
            if (current instanceof ServiceConfigurationError &&
                    (current.getMessage().contains("not found") ||
                            current.getMessage().contains("cannot be loaded"))) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    public ServiceFiles<T> add(Class<? extends T> clazz) {
        this.serviceTypes.add(clazz);
        return this;
    }

    public ServiceFiles<T> ignoreClassNotFound(boolean ignore) {
        this.ignoreClassNotFound = ignore;
        return this;
    }
}
