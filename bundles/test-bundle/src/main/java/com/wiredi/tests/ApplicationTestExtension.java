package com.wiredi.tests;

import com.wiredi.runtime.Environment;
import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.WiredApplication;
import com.wiredi.runtime.WiredApplicationInstance;
import com.wiredi.runtime.domain.WireContainerCallback;
import com.wiredi.runtime.domain.provider.SimpleProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.lang.Ordered;
import com.wiredi.runtime.properties.Key;
import com.wiredi.runtime.properties.TypedProperties;
import com.wiredi.runtime.time.Timed;
import com.wiredi.tests.instance.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.extension.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationTestExtension extends AbstractTestExtension implements AfterAllCallback, TestInstanceFactory, ParameterResolver {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationTestExtension.class);
    private static final TestInstanceCache<WiredApplicationInstance> INSTANCE_CACHE = new TestInstanceCache<>(ApplicationTestExtension::newApplication);

    @Nullable
    public static WiredApplicationInstance getApplicationInstance(ExtensionContext context) {
        return INSTANCE_CACHE.get(context);
    }

    public WiredApplicationInstance applicationInstanceOf(ExtensionContext context) {
        return INSTANCE_CACHE.getOrCreate(context);
    }

    private static WiredApplicationInstance newApplication(InstanceIdentifier identifier) {
        logger.debug("Creating new WiredApplication");
        return WiredApplication.start(wireContainer -> {
            wireContainer.announce(
                    SimpleProvider.builder(new ApplicationIdentifierWireRepositoryContextCallbacks(identifier))
                            .withAdditionalType(WireContainerCallback.class)
                            .withOrder(Ordered.LAST - 10)
                            .withSingleton(true)
            );
        });
    }

    @Override
    public @NotNull WireContainer wireContainerOf(ExtensionContext context) {
        return applicationInstanceOf(context).wireContainer();
    }

    @Override
    public Object createTestInstance(
            TestInstanceFactoryContext factoryContext,
            ExtensionContext extensionContext
    ) throws TestInstantiationException {
        WiredApplicationInstance applicationInstance = applicationInstanceOf(extensionContext);

        Object instance = applicationInstance.wireContainer()
                .tryGet((Class<Object>) factoryContext.getTestClass())
                .orElseGet(() -> applicationInstance.wireContainer().onDemandInjector().get(factoryContext.getTestClass()));
        try {
            beforeAll(extensionContext);
        } catch (Exception e) {
            throw new TestInstantiationException("Error while executing beforeAll hook: " + e.getLocalizedMessage(), e);
        }

        return instance;
    }

    @Override
    public boolean supportsParameter(
            ParameterContext parameterContext,
            ExtensionContext extensionContext
    ) throws ParameterResolutionException {
        TypeIdentifier<Object> typeIdentifier = TypeIdentifier.of(parameterContext.getParameter().getParameterizedType());
        WiredApplicationInstance applicationInstance = applicationInstanceOf(extensionContext);

        return applicationInstance.wireContainer().contains(typeIdentifier);
    }

    @Override
    public Object resolveParameter(
            ParameterContext parameterContext,
            ExtensionContext extensionContext
    ) throws ParameterResolutionException {
        TypeIdentifier<Object> typeIdentifier = TypeIdentifier.of(parameterContext.getParameter().getParameterizedType());
        WiredApplicationInstance applicationInstance = applicationInstanceOf(extensionContext);
        WireContainer repository = applicationInstance.wireContainer();

        if (typeIdentifier.isNativeProvider()) {
            return repository.getNativeProvider(typeIdentifier.getGenericTypes().getFirst());
        } else {
            return repository.get(typeIdentifier);
        }
    }

    private record ApplicationIdentifierWireRepositoryContextCallbacks(
            InstanceIdentifier identifier
    ) implements WireContainerCallback {

        @Override
        public void loadedEnvironment(@NotNull Timed timed, @NotNull Environment environment) {
            for (String file : identifier.properties().files()) {
                TypedProperties properties = environment.loadProperties(file);
                environment.setProperties(properties);
            }

            for (Prop property : identifier.properties().properties()) {
                environment.setProperty(Key.format(property.key()), property.value());
            }
        }
    }
}
