package com.wiredi.runtime.security.authentication;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A general extractor that aggregates known {@link AuthenticationProvider} instances associated with the source type.
 * <p>
 * This class can easily be used to register {@link AuthenticationProvider} and later retrieve authentication from source.
 */
public class AuthenticationExtractor {

    private final List<AuthenticationProvider> providers = new ArrayList<>();

    public AuthenticationExtractor(List<AuthenticationProvider> authenticationFactories) {
        this.providers.addAll(authenticationFactories);
    }

    /**
     * Try to retrieve the {@link Authentication} from the source.
     * <p>
     * If any {@link AuthenticationProvider} is able to retrieve an {@link Authentication} from the {@code source}, this
     * method returns it here.
     * However, if no {@link AuthenticationProvider} is able to retrieve an {@link Authentication}, null is returned.
     *
     * @param source the source from which the {@link Authentication} should be retrieved.
     * @param <T>    the generic source type
     * @return the {@link Authentication} for the {@code source}, or null if no {@link AuthenticationProvider} could determine an authentication
     */
    @Nullable
    public <T> Authentication getAuthentication(T source) {
        for (AuthenticationProvider provider : providers) {
            if (provider.canExtractFrom(source)) {
                Authentication extracted = provider.extract(source);
                if (extracted != null) {
                    return extracted;
                }
            }
        }

        return null;
    }
}
