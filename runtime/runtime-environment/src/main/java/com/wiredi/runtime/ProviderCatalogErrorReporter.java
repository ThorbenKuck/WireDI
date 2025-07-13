package com.wiredi.runtime;

import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public interface ProviderCatalogErrorReporter {

    void report(Map<IdentifiableProvider<?>, List<Throwable>> errors);

    class Default implements ProviderCatalogErrorReporter {

        private static final Logger logger = LoggerFactory.getLogger(ProviderCatalogErrorReporter.class);

        @Override
        public void report(Map<IdentifiableProvider<?>, List<Throwable>> errors) {
            logger.error("Error in " + errors.size() + " providers, during provider catalog initialization.");
            errors.forEach((provider, throwables) -> {
                logger.error("Provider: " + provider.getClass().getName() + " had " + throwables.size() + " errors.");
                throwables.forEach(throwable -> logger.error("[" + provider.type() + "]" + throwable.getMessage(), throwable));
            });
        }
    }
}
