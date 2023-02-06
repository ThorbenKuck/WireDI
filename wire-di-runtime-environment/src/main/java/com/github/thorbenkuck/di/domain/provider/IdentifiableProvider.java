package com.github.thorbenkuck.di.domain.provider;

import com.github.thorbenkuck.di.runtime.WireRepository;
import com.github.thorbenkuck.di.domain.WireCapable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public interface IdentifiableProvider<T> extends Comparable<IdentifiableProvider<?>>, WireCapable {

	int DEFAULT_PRIORITY = 0;

	/**
	 * Defines the type, this IdentifiableProvider will produce.
	 *
	 * This type returned right here must be assignable from the type the {@link #get(WireRepository) get method}
	 * returns. If not, an exception will be raised. To prevent this, the method {@link #bypassSanityCheck()} may
	 * return true. Use with cation.
	 *
	 * @return the type this IdentifiableProvider produces
	 * @see #bypassSanityCheck()
	 */
	@NotNull
	Class<?> type();

	/**
	 * Returns, whether the type produced by this IdentifiableProvider is singleton or produced on request.
	 *
	 * @return true, if the same instance is returned with ever call of the {@link #get(WireRepository) get method}
	 */
	boolean isSingleton();

	/**
	 * This method produces the instance associated with the {@link #type() type method}.
	 *
	 * If the method {@link #isSingleton()} returns true, it is expected that this method returns the same instance
	 * every time. If not, it is expected that calling this method creates a new instance every time it is called.
	 *
	 * To resolve dependencies, the WireRepository instance this IdentifiableProvider is created through is passed
	 * into this method.
	 *
	 * @param wiredRepository the {@link WireRepository wireRepository} instance this Provider is created through
	 * @return the instance, which might be null
	 */
	@Nullable
	T get(@NotNull final WireRepository wiredRepository);

	/**
	 * The priority of this IdentifiableProvider, which might be used in the {@link com.github.thorbenkuck.di.domain.WireConflictResolver}
	 * and is used in {@link com.github.thorbenkuck.di.domain.WireConflictStrategy#BEST_MATCH}
	 *
	 * @return the priority of this IdentifiableProvider
	 */
	default int priority() {
		return DEFAULT_PRIORITY;
	}

	@Override
	default int compareTo(@NotNull final IdentifiableProvider<?> that) {
		return Integer.compare(that.priority(), priority());
	}

	default boolean bypassSanityCheck() { return false; }

	// ########################################################################
	// ### Static methods, to wrap non-instances into Identifiable Provider ###
	// ########################################################################

	static <T> IdentifiableProvider<T> singleton(T t) {
		return new SingletonInstanceIdentifiableProvider<>(t);
	}

	static <T> IdentifiableProvider<T> singleton(T t, Class<?>... types) {
		return new SingletonInstanceIdentifiableProvider<>(t, types);
	}

	static <T> IdentifiableProvider<T> wrap(Supplier<T> supplier, Class<T> type) {
		return new MultitonGenericIdentifiableProvider<>((r) -> supplier.get(), new TypeIdentifier[]{ TypeIdentifier.of(type) }, type);
	}

	static <T> IdentifiableProvider<T> wrap(Supplier<T> supplier, Class<T> type, TypeIdentifier<?>[] wireTypes) {
		return new MultitonGenericIdentifiableProvider<>((r) -> supplier.get(), wireTypes, type);
	}

	static <T> IdentifiableProvider<T> wrap(Function<WireRepository, T> function, Class<T> type) {
		return new MultitonGenericIdentifiableProvider<>(function, new TypeIdentifier[]{ TypeIdentifier.of(type) }, type);
	}

	static <T> IdentifiableProvider<T> wrap(Function<WireRepository, T> function, Class<T> type, TypeIdentifier<?>[] wireTypes) {
		return new MultitonGenericIdentifiableProvider<>(function, wireTypes, type);
	}

	static <T> IdentifiableProvider<T> wrapSingleton(Supplier<T> supplier, Class<T> type) {
		return new SingletonGenericIdentifiableProvider<>((r) -> supplier.get(), new TypeIdentifier[]{ TypeIdentifier.of(type) }, type);
	}

	static <T> IdentifiableProvider<T> wrapSingleton(Supplier<T> supplier, Class<T> type, TypeIdentifier<?>[] wireTypes) {
		return new SingletonGenericIdentifiableProvider<>((r) -> supplier.get(), wireTypes, type);
	}

	static <T> IdentifiableProvider<T> wrapSingleton(Function<WireRepository, T> function, Class<T> type) {
		return new SingletonGenericIdentifiableProvider<>(function, new TypeIdentifier[]{ TypeIdentifier.of(type) }, type);
	}

	static <T> IdentifiableProvider<T> wrapSingleton(Function<WireRepository, T> function, Class<T> type, TypeIdentifier<?>[] wireTypes) {
		return new SingletonGenericIdentifiableProvider<>(function, wireTypes, type);
	}
}
