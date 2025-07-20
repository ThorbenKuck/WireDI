package com.wiredi.tests;

import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.tests.instance.InstanceIdentifier;
import com.wiredi.tests.instance.TestInstanceCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.extension.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WiredTestExtension extends AbstractTestExtension implements TestInstanceFactory, TestInstancePreDestroyCallback, ParameterResolver {

    private static final Logger logger = LoggerFactory.getLogger(WiredTestExtension.class);
    private static final TestInstanceCache<WireContainer> INSTANCE_CACHE = new TestInstanceCache<>(WiredTestExtension::newContainer);

    private static WireContainer newContainer(InstanceIdentifier identifier) {
        logger.debug("Creating new WireContainer");
        return WireContainer.open();
    }

    @Nullable
    public static WireContainer getWireContainer(ExtensionContext context) {
        return INSTANCE_CACHE.get(context);
    }

    @Override
    public Object createTestInstance(
            TestInstanceFactoryContext factoryContext,
            ExtensionContext extensionContext
    ) throws TestInstantiationException {
        WireContainer container = wireContainerOf(extensionContext);

        Object instance = container.tryGet((Class<Object>) factoryContext.getTestClass())
                .orElseGet(() -> container.onDemandInjector().get(factoryContext.getTestClass()));
        try {
            beforeAll(extensionContext);
        } catch (Exception e) {
            throw new TestInstantiationException("Error while executing beforeAll hook: " + e.getLocalizedMessage(), e);
        }
        return instance;
    }

    @Override
    public void preDestroyTestInstance(ExtensionContext context) throws Exception {
        WireContainer container = INSTANCE_CACHE.get(context);
        Exception error = null;
        if (container != null) {
            try {
                afterAll(context);
            } catch (Exception e) {
                error = new IllegalStateException("Error while executing afterAll hook: " + e.getLocalizedMessage(), e);
            }
            try {
                container.clear();
            } catch (Exception e) {
                if (error != null) {
                    error.addSuppressed(e);
                }
                throw e;
            } finally {
                INSTANCE_CACHE.remove(context);
            }
        }

        if (error != null) {
            throw error;
        }
    }

    @Override
    public boolean supportsParameter(
            ParameterContext parameterContext,
            ExtensionContext extensionContext
    ) throws ParameterResolutionException {
        TypeIdentifier<Object> typeIdentifier = TypeIdentifier.of(parameterContext.getParameter().getParameterizedType());
        WireContainer container = wireContainerOf(extensionContext);

        return container.contains(typeIdentifier);
    }

    @Override
    public Object resolveParameter(
            ParameterContext parameterContext,
            ExtensionContext extensionContext
    ) throws ParameterResolutionException {
        TypeIdentifier<Object> typeIdentifier = TypeIdentifier.of(parameterContext.getParameter().getParameterizedType());
        WireContainer container = wireContainerOf(extensionContext);

        if (typeIdentifier.isNativeProvider()) {
            return container.getNativeProvider(typeIdentifier.getGenericTypes().getFirst());
        } else {
            return container.get(typeIdentifier);
        }
    }

    @Override
    public @NotNull WireContainer wireContainerOf(ExtensionContext context) {
        return INSTANCE_CACHE.getOrCreate(context);
    }
}
