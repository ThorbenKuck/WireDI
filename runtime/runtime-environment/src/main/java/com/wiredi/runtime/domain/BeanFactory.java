package com.wiredi.runtime.domain;

import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.domain.factories.Bean;
import com.wiredi.runtime.domain.factories.EmptyBeanFactory;
import com.wiredi.runtime.domain.factories.SimpleBeanFactory;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.QualifiedTypeIdentifier;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.qualifier.QualifierType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * A factory for creating {@link Bean} instances.
 * <p>
 * A BeanFactory is responsible for creating {@link Bean} instances for a given type.
 * Each different type has its own factory.
 * An instance is asked by a {@link Scope} to create a new instance of a bean if no cached instance is available.
 * Depending on the scope, the factory may also create a new instance if the cached instance is no longer valid.
 * <p>
 * This interface allows you to modify how beans are created.
 * By default, the {@link SimpleBeanFactory} is used, which creates new instances of the given type.
 * If the behavior of the factory should be changed, you can create a custom implementation and provide it to your scope.
 * You can also have different factories for different scopes.
 * <p>
 * Factories are clustered by Type, meaning that every type has its own factory.
 * Given the example that we have a {@code Coffee} interface with the implementation {@code Arabica} and {@code Robusta},
 * we'd have 3 factories.
 * One for creating {@code Arabica}, one for creating {@code Robusta} and one for creating {@code Coffee}.
 *
 * @param <T>
 * @see Scope
 */
public interface BeanFactory<T> {

    static <T> BeanFactory<T> empty() {
        return EmptyBeanFactory.INSTANCE;
    }

    static <T> BeanFactory<T> empty(TypeIdentifier<T> typeIdentifier) {
        return new EmptyBeanFactory<>(typeIdentifier);
    }

    static <T> BeanFactory<T> of(TypeIdentifier<T> typeIdentifier) {
        return new SimpleBeanFactory<>(typeIdentifier);
    }

    @NotNull TypeIdentifier<T> rootType();

    @NotNull
    default Collection<Bean<T>> getAll(@NotNull WireContainer wireContainer) {
        return getAll(wireContainer, rootType());
    }

    @NotNull Collection<Bean<T>> getAll(@NotNull WireContainer wireContainer, @NotNull TypeIdentifier<T> type);

    @Nullable Bean<T> get(
            @NotNull WireContainer wireContainer,
            @NotNull TypeIdentifier<T> type
    );

    @Nullable Bean<T> get(
            @NotNull WireContainer wireContainer,
            @NotNull QualifiedTypeIdentifier<T> type
    );

    void register(@NotNull IdentifiableProvider<T> identifiableProvider);

    @Nullable
    IdentifiableProvider<T> resolveProvider(@Nullable QualifierType qualifier);
}
