package com.wiredi.domain.provider;

import com.wiredi.domain.Ordered;
import com.wiredi.domain.WireConflictResolver;
import com.wiredi.domain.WireConflictStrategy;
import com.wiredi.qualifier.QualifierType;
import com.wiredi.runtime.WireRepository;
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

	static <T> IdentifiableProvider<T> singleton(T t) {
		return singleton(t, TypeIdentifier.resolve(t));
	}

	static <T, S extends T> IdentifiableProvider<T> singleton(S instance, Class<T> type) {
		return singleton(instance, TypeIdentifier.of(type));
	}

	static <T, S extends T> IdentifiableProvider<T> singleton(S instance, TypeIdentifier<T> type) {
		return SingletonInstanceIdentifiableProvider.of(instance, type);
	}

	static <T> IdentifiableProvider<T> wrap(Supplier<T> supplier, TypeIdentifier<T> type) {
		return wrap(supplier, type, List.of(type));
	}

	static <T> IdentifiableProvider<T> wrap(Supplier<T> supplier, TypeIdentifier<T> type, List<TypeIdentifier<?>> wireTypes) {
		return wrap((r) -> supplier.get(), type, wireTypes);
	}

	static <T> IdentifiableProvider<T> wrap(Function<WireRepository, T> function, TypeIdentifier<T> type) {
		return wrap(function, type, List.of(type));
	}

	static <T> IdentifiableProvider<T> wrap(Function<WireRepository, T> function, TypeIdentifier<T> type, List<TypeIdentifier<?>> wireTypes) {
		return new MultiTonGenericIdentifiableProvider<>(function, wireTypes, type);
	}

	static <T> IdentifiableProvider<T> wrapSingleton(Supplier<T> supplier, TypeIdentifier<T> type) {
		return wrapSingleton(supplier, type, List.of(type));
	}

	static <T> IdentifiableProvider<T> wrapSingleton(Supplier<T> supplier, TypeIdentifier<T> type, List<TypeIdentifier<?>> wireTypes) {
		return wrapSingleton((r) -> supplier.get(), type, wireTypes);
	}

	static <T> IdentifiableProvider<T> wrapSingleton(Function<WireRepository, T> function, TypeIdentifier<T> type) {
		return wrapSingleton(function, type, List.of(type));
	}

	static <T> IdentifiableProvider<T> wrapSingleton(Function<WireRepository, T> function, TypeIdentifier<T> type, List<TypeIdentifier<?>> wireTypes) {
		return new SingletonGenericIdentifiableProvider<>(function, wireTypes, type);
	}

	// #################################
	// ### Concrete member functions ###
	// #################################

	/**
	 * Defines the type, this IdentifiableProvider will produce.
	 * <p>
	 * This type returned right here must be assignable from the type the {@link #get(WireRepository) get method}
	 * returns.
	 *
	 * @return the type this IdentifiableProvider produces
	 */
	@NotNull
	TypeIdentifier<T> type();

	@NotNull
	default List<TypeIdentifier<?>> additionalWireTypes() {
		return Collections.emptyList();
	}

	/**
	 * Returns, whether the type produced by this IdentifiableProvider is singleton or produced on request.
	 *
	 * @return true, if the same instance is returned with ever call of the {@link #get(WireRepository) get method}
	 */
	boolean isSingleton();

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
	 * @param wiredRepository the {@link WireRepository wireRepository} instance this Provider is created through
	 * @return the instance, which might be null
	 */
	@Nullable
	T get(@NotNull final WireRepository wiredRepository);

	/**
	 * The priority of this IdentifiableProvider, which might be used in the {@link WireConflictResolver}
	 * and is used in {@link WireConflictStrategy#BEST_MATCH}
	 *
	 * @return the priority of this IdentifiableProvider
	 */
	default int getOrder() {
		return FIRST;
	}

	@NotNull
	default List<QualifierType> qualifiers() {
		return Collections.emptyList();
	}
}
