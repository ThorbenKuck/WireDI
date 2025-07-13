package com.wiredi.runtime.domain.factories;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.lang.Ordered;
import org.jetbrains.annotations.NotNull;

public record Bean<T>(
        @NotNull T instance,
        @NotNull IdentifiableProvider<T> provider
) implements Ordered {

    private static final Logging logger = Logging.getInstance(Bean.class);

    public void tearDown() {
        try {
            provider.tearDown(instance);
        } catch (Exception e) {
            logger.error("Failed to tear down bean " + provider.type(), e);
        }
    }

    @Override
    public int getOrder() {
        if (instance instanceof Ordered o) {
            return o.getOrder();
        } else {
            return provider.getOrder();
        }
    }
}
