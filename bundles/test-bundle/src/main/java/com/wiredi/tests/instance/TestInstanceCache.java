package com.wiredi.tests.instance;

import com.wiredi.tests.TestPropertiesInstance;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class TestInstanceCache<T> {

    private final Map<Object, T> cache = new ConcurrentHashMap<>();
    private final Function<InstanceIdentifier, T> factory;
    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(TestInstanceCache.class);

    public TestInstanceCache(Function<InstanceIdentifier, T> factory) {
        this.factory = factory;
    }

    public T getOrCreate(ExtensionContext extensionContext) {
        return (T) extensionContext.getStore(NAMESPACE)
                .getOrComputeIfAbsent("INSTANCE", k -> doGetOrCreate(extensionContext));
    }

    private T doGetOrCreate(ExtensionContext extensionContext) {
        TestPropertiesInstance properties = getProperties(extensionContext);
        InstanceIdentifier identifier = new InstanceIdentifier(properties);

        if (extensionContext.getRequiredTestClass().isAnnotationPresent(Standalone.class)) {
            return factory.apply(identifier);
        }

        return cache.computeIfAbsent(identifier, key -> factory.apply(identifier));
    }

    public void remove(ExtensionContext context) {
        TestPropertiesInstance properties = getProperties(context);
        InstanceIdentifier identifier = new InstanceIdentifier(properties);
        cache.remove(identifier);
    }

    public T get(ExtensionContext context) {
        TestPropertiesInstance properties = getProperties(context);
        InstanceIdentifier identifier = new InstanceIdentifier(properties);

        return cache.get(identifier);
    }

    private TestPropertiesInstance getProperties(ExtensionContext extensionContext) {
        return extensionContext.getTestClass()
                .map(it -> it.getAnnotation(TestProperties.class))
                .map(TestPropertiesInstance::of)
                .orElse(TestPropertiesInstance.empty());
    }
}
