package com.wiredi.runtime.security.authentication;

import com.wiredi.runtime.lang.Ordered;
import org.jetbrains.annotations.Nullable;

/**
 * A provider, extracting {@link Authentication} from any kind of source data.
 * <p>
 * Implementations of this interface are responsible for extracting {@link Authentication} from different sources.
 * There could be an {@link AuthenticationProvider} for the {@link java.net.http.HttpRequest}, that retrieves a username/password
 * authentication from request parameters.
 * Another implementation could retrieve a token authentication from the {@link java.net.http.HttpRequest}.
 * Yet another implementation could retrieve a token authentication from a {@code kafka.ConsumerRecord}.
 * <p>
 * Implementations are technology specific.
 */
public interface AuthenticationProvider extends Ordered {

    /**
     * Try to extract the [Authentication] from the source.
     * <p>
     * This method is only invoked if the `source` is of the method [#canExtractFrom(Object)] returned true.
     *
     * @param source the source from which the [Authentication] should be extracted
     * @return the authentication associated with the source, or null if non could be extracted
     */
    @Nullable
    Authentication extract(Object source);

    /**
     * This method determines, if the provider accepts the {@code source}
     *
     * @param source the source to handle
     * @return true, if the {@link #extract(Object)} method can process the source, otherwise false
     */
    default boolean canExtractFrom(Object source) {
        return true;
    }
}
