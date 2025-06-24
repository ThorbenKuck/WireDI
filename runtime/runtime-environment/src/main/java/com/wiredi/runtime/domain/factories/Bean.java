package com.wiredi.runtime.domain.factories;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import org.jetbrains.annotations.NotNull;

public record Bean<T>(
        @NotNull T instance,
        @NotNull IdentifiableProvider<T> provider
) {

    private static final Logging logger = Logging.getInstance(Bean.class);

    public void tearDown() {
        try {
            provider.tearDown(instance);
        } catch (Exception e) {
            logger.error("Failed to tear down bean " + provider.type(), e);
        }
    }
}
