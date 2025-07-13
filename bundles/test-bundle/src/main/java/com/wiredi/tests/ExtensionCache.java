package com.wiredi.tests;

import com.wiredi.runtime.Environment;
import com.wiredi.runtime.WiredApplication;
import com.wiredi.runtime.WiredApplicationInstance;
import com.wiredi.runtime.domain.WireContainerCallback;
import com.wiredi.runtime.domain.provider.SimpleProvider;
import com.wiredi.runtime.lang.Ordered;
import com.wiredi.runtime.properties.Key;
import com.wiredi.runtime.properties.TypedProperties;
import com.wiredi.runtime.time.Timed;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.HashMap;
import java.util.Map;

public class ExtensionCache {

    private static final Map<ApplicationIdentifier, WiredApplicationInstance> cache = new HashMap<>();

    public static WiredApplicationInstance getOrCreate(ExtensionContext extensionContext) {
        if (extensionContext.getRequiredTestClass().isAnnotationPresent(Standalone.class)) {
            return WiredApplication.start();
        }

        TestPropertiesInstance properties = getProperties(extensionContext);
        return cache.computeIfAbsent(new ApplicationIdentifier(properties), ExtensionCache::newApplication);
    }

    private static WiredApplicationInstance newApplication(ApplicationIdentifier identifier) {
        return WiredApplication.start(wireRepository -> {
            wireRepository.announce(
                    SimpleProvider.builder(new ApplicationIdentifierWireRepositoryContextCallbacks(identifier))
                            .withAdditionalType(WireContainerCallback.class)
                            .withOrder(Ordered.LAST - 10)
                            .withSingleton(true)
            );
        });
    }

    private static TestPropertiesInstance getProperties(ExtensionContext extensionContext) {
        TestProperties annotation = extensionContext.getRequiredTestClass()
                .getAnnotation(TestProperties.class);

        if (annotation == null) {
			return TestPropertiesInstance.empty();
        }

        return TestPropertiesInstance.of(annotation);
    }

    private record ApplicationIdentifierWireRepositoryContextCallbacks(
            ApplicationIdentifier identifier
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
