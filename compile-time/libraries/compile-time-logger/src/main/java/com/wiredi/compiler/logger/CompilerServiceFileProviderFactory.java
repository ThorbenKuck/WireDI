package com.wiredi.compiler.logger;

import com.wiredi.runtime.ServiceFiles;
import com.wiredi.runtime.values.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CompilerServiceFileProviderFactory implements ServiceFiles.ProviderFactory {

    @SuppressWarnings("rawtypes")
    private final Map<Class, List> services = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(CompilerServiceFileProviderFactory.class);

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<ServiceLoader.Provider<T>> getProviders(Class<T> type) {
        return (List<ServiceLoader.Provider<T>>) services.computeIfAbsent(type, this::loadServices);
    }

    public <T> List<ServiceLoader.Provider<T>> loadServices(Class<T> type) {
        List<ServiceLoader.Provider<T>> providers = new ArrayList<>();

        try {
            Enumeration<URL> serviceFiles = getClass().getClassLoader()
                    .getResources("META-INF/services/" + type.getName());

            while (serviceFiles.hasMoreElements()) {
                URL serviceFile = serviceFiles.nextElement();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(serviceFile.openStream(), StandardCharsets.UTF_8))) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty() && !line.startsWith("#")) {
                            providers.add(new CompilerProvider<>(line));
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading services");
            e.printStackTrace();
        }

        return providers;
    }

    private class CompilerProvider<T> implements ServiceLoader.Provider<T> {

        private final Value<Class<T>> type;
        private final Value<T> instance;

        private CompilerProvider(String className) {
            this.type = Value.lazy(() -> (Class<T>) Class.forName(className));
            this.instance = Value.lazy(() -> type.get().getDeclaredConstructor().newInstance());
        }

        @Override
        public Class<? extends T> type() {
            try {
                return type.get();
            } catch (Throwable t) {
                throw new ServiceConfigurationError("Unable to load class for service " + type + ": " + t.getMessage(), t);
            }
        }

        @Override
        public T get() {
            try {
                return instance.get();
            } catch (Throwable t) {
                throw new ServiceConfigurationError("Unable to load instance for service " + type + ": " + t.getMessage(), t);
            }
        }
    }
}
