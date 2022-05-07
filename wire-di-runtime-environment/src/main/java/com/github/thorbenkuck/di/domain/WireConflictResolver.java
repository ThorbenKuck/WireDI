package com.github.thorbenkuck.di.domain;

import com.github.thorbenkuck.di.domain.provider.IdentifiableProvider;
import com.github.thorbenkuck.di.runtime.exceptions.DiInstantiationException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface WireConflictResolver {
    /**
     * Finds a single {@link IdentifiableProvider} in a list of multiple instances.
     *
     * How exactly the single instance is found is determined by the implementation of this interface.
     *
     * @param providerList the list of {@link IdentifiableProvider} to find one single of.
     * @param expectedType the expected type of ths instance.
     * @param <T> the generic parameter of the expected time.
     * @return the resolved {@link IdentifiableProvider}
     * @throws DiInstantiationException if this method could not resolve a single {@link IdentifiableProvider}
     * @see #error(List, int, Class)
     */
    @NotNull
    <T> IdentifiableProvider<T> find(
            @NotNull final List<IdentifiableProvider<T>> providerList,
            @NotNull final Class<T> expectedType
    );

    /**
     * The name of the WireConflictResolver, used to display in errors.
     *
     * Used for identification, there is no requirement for it to be unique, although it makes
     * sense to have it uniquely identify your resolver.
     *
     * When using enums as WireConflictResolver, you do not need to worry about this method.
     *
     * @return the name of this WireConflictResolver
     */
    String name();

    /**
     * Throws a DiInstantiationException, build by the {@link #buildError(List, int, Class)} method.
     *
     * The return value is {@link IdentifiableProvider}, to allow for authentic integration. However, please
     * note that this method will never return anything, but instead always throw an {@link DiInstantiationException}
     *
     * @param total the list to search in
     * @param match the amount of matches finds in the list
     * @param type the searched for type
     * @param <T> the generic type of the searched for type
     * @return NOTHING, will always throw an exception
     * @throws DiInstantiationException always, with a message build with {@link #buildError(List, int, Class)}
     * @see #buildError(List, int, Class)
     */
    @NotNull
    default <T> IdentifiableProvider<T> error(
            @NotNull final List<IdentifiableProvider<T>> total,
            final int match,
            @NotNull final Class<T> type
    ) throws DiInstantiationException {
        throw buildError(total, match, type);
    }

    /**
     * Builds an error message, that this conflict resolver could not resolve one single {@link IdentifiableProvider}
     *
     * @param total the list to search in
     * @param match the amount of matches finds in the list
     * @param type the searched for type
     * @param <T> the generic type of the searched for type
     * @return an instance of {@link DiInstantiationException} to throw
     */
    @NotNull
    default <T> DiInstantiationException buildError(
            @NotNull final List<IdentifiableProvider<T>> total,
            final int match,
            @NotNull final Class<T> type
    ) {
        final StringBuilder result = new StringBuilder();
        result.append("Expected to find exactly 1 Provider for type ")
                .append(type)
                .append(" using the ")
                .append(name())
                .append(" strategy, but got ")
                .append(match)
                .append(" out of ")
                .append(total.size())
                .append(" potential candidates")
                .append(System.lineSeparator())
                .append("Candidates: ")
                .append(System.lineSeparator());

        total.forEach(provider -> result.append(" - ").append(provider.toString()).append(System.lineSeparator()));
        return new DiInstantiationException(result.toString(), type);
    }
}
