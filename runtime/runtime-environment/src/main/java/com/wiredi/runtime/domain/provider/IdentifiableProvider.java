package com.wiredi.runtime.domain.provider;

import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.domain.ScopeProvider;
import com.wiredi.runtime.lang.Ordered;
import com.wiredi.runtime.domain.StandardWireConflictResolver;
import com.wiredi.runtime.domain.WireConflictResolver;
import com.wiredi.runtime.domain.provider.condition.LoadCondition;
import com.wiredi.runtime.qualifier.QualifierType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public interface IdentifiableProvider<T> extends Ordered {

    // ########################################################################
    // ### Static methods, to wrap non-instances into Identifiable Provider ###
    // ########################################################################

    static <T> SingletonInstanceIdentifiableProvider<T> singleton(T t) {
        return singleton(t, TypeIdentifier.resolve(t));
    }

    static <T, S extends T> SingletonInstanceIdentifiableProvider<T> singleton(S instance, Class<T> type) {
        return singleton(instance, TypeIdentifier.of(type));
    }

    static <T, S extends T> SingletonInstanceIdentifiableProvider<T> singleton(S instance, TypeIdentifier<T> type) {
        return SingletonInstanceIdentifiableProvider.of(instance, type);
    }

    static <T> MultiTonGenericIdentifiableProvider<T> wrap(Supplier<T> supplier, TypeIdentifier<T> type) {
        return wrap(supplier, type, List.of(type));
    }

    static <T> MultiTonGenericIdentifiableProvider<T> wrap(Supplier<T> supplier, TypeIdentifier<T> type, List<TypeIdentifier<?>> wireTypes) {
        return wrap((r) -> supplier.get(), type, wireTypes);
    }

    static <T> MultiTonGenericIdentifiableProvider<T> wrap(Function<WireContainer, T> function, TypeIdentifier<T> type) {
        return wrap(function, type, List.of(type));
    }

    static <T> MultiTonGenericIdentifiableProvider<T> wrap(Function<WireContainer, T> function, TypeIdentifier<T> type, List<TypeIdentifier<?>> wireTypes) {
        return new MultiTonGenericIdentifiableProvider<>(function, wireTypes, type);
    }

    static <T> LazySingletonIdentifiableProvider<T> wrapSingleton(Supplier<T> supplier, TypeIdentifier<T> type) {
        return wrapSingleton(supplier, type, List.of(type));
    }

    static <T> LazySingletonIdentifiableProvider<T> wrapSingleton(Supplier<T> supplier, TypeIdentifier<T> type, List<TypeIdentifier<?>> wireTypes) {
        return wrapSingleton((r) -> supplier.get(), type, wireTypes);
    }

    static <T> LazySingletonIdentifiableProvider<T> wrapSingleton(Function<WireContainer, T> function, TypeIdentifier<T> type) {
        return wrapSingleton(function, type, List.of(type));
    }

    static <T> LazySingletonIdentifiableProvider<T> wrapSingleton(Function<WireContainer, T> function, TypeIdentifier<T> type, List<TypeIdentifier<?>> wireTypes) {
        return new LazySingletonIdentifiableProvider<>(function, wireTypes, type);
    }

    /**
     * Creates a new builder for a SimpleProvider.
     *
     * @param typeIdentifier the type identifier this provider will provide
     * @param <T>            the type parameter
     * @return a new builder
     */
    static <T> SimpleProvider.Builder<T> builder(TypeIdentifier<T> typeIdentifier) {
        return SimpleProvider.builder(typeIdentifier);
    }

    /**
     * Creates a new builder for a SimpleProvider.
     *
     * @param type the class type this provider will provide
     * @param <T>  the type parameter
     * @return a new builder
     */
    static <T> SimpleProvider.Buildable<T> builder(Class<T> type) {
        return SimpleProvider.builder(TypeIdentifier.of(type));
    }

    /**
     * Creates a new builder for a SimpleProvider.
     *
     * @param instance the instance to use
     * @param <T>      the type parameter
     * @return a new builder
     */
    static <T> SimpleProvider.Builder<T> builder(T instance) {
        SimpleProvider.Buildable<T> buildable = (SimpleProvider.Buildable<T>) builder(instance.getClass());
        return buildable.withInstance(instance);
    }

    // #################################
    // ### Concrete member functions ###
    // #################################

    /**
     * Defines the type, this IdentifiableProvider will produce.
     * <p>
     * This type returned right here must be assignable from the type the {@link #get(WireContainer) get method}
     * returns.
     *
     * @return the type this IdentifiableProvider produces
     */
    @NotNull
    TypeIdentifier<? super T> type();

    @NotNull
    default List<TypeIdentifier<?>> additionalWireTypes() {
        return Collections.emptyList();
    }

    /**
     * Returns, whether the type produced by this IdentifiableProvider is singleton or produced on request.
     *
     * @return true, if the same instance is returned with ever call of the {@link #get(WireContainer) get method}
     */
    default boolean isSingleton() {
        return true;
    }

    /**
     * If this provider is the primary provider of its {@link #type()} and {@link #additionalWireTypes()}.
     * <p>
     * If true, it will override other existing providers when injection qualifiers are resolved.
     *
     * @return true, if {@link #get(WireContainer)} should return the primary instance for the {@link #type()} and {@link #additionalWireTypes()}.
     */
    default boolean primary() {
        return false;
    }

    /**
     * This method produces the instance associated with the {@link #type() type method}.
     * <p>
     * If the method {@link #isSingleton()} returns true, it is expected that this method returns the same instance
     * every time. If not, it is expected that calling this method creates a new instance every time it is called.
     * <p>
     * To resolve dependencies, the WireRepository instance this IdentifiableProvider is created through is passed
     * into this method.
     *
     * @param wireContainer the {@link WireContainer wireContainer} instance this Provider is created through
     * @param concreteType the type that was requested
     * @return the instance, which might be null
     */
    @Nullable
    T get(@NotNull final WireContainer wireContainer, @NotNull final TypeIdentifier<T> concreteType);

    @Nullable
    default T get(@NotNull final WireContainer wireContainer) {
        return get(wireContainer, (TypeIdentifier<T>) type());
    }

    /**
     * The priority of this IdentifiableProvider, which might be used in the {@link WireConflictResolver}
     * and is used in {@link StandardWireConflictResolver#BEST_MATCH}
     *
     * @return the priority of this IdentifiableProvider
     */
    @Override
    default int getOrder() {
        return DEFAULT;
    }

    @NotNull
    default List<@NotNull QualifierType> qualifiers() {
        return Collections.emptyList();
    }

    @Nullable
    default LoadCondition condition() {
        return null;
    }

    @Nullable
    default ScopeProvider scope() {
        return null;
    }

    default void tearDown(@NotNull T t) {
        // Default, do nothing.
    }
}
