package com.github.thorbenkuck.di.runtime;

import com.github.thorbenkuck.di.annotations.ManualWireCandidate;
import com.github.thorbenkuck.di.aspects.AspectRepository;
import com.github.thorbenkuck.di.domain.WireConflictResolver;
import com.github.thorbenkuck.di.domain.WireConflictStrategy;
import com.github.thorbenkuck.di.domain.provider.TypeIdentifier;
import com.github.thorbenkuck.di.domain.provider.IdentifiableProvider;
import com.github.thorbenkuck.di.runtime.exceptions.DiInstantiationException;
import com.github.thorbenkuck.di.runtime.properties.TypedProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Provider;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ManualWireCandidate
public interface WireRepository {

	static WireRepository open() {
		WiredTypes wiredTypes = new WiredTypes();
		wiredTypes.load();

		return wiredTypes;
	}

	static WireRepository create() {
		return new WiredTypes();
	}

	boolean isLoaded();

	@NotNull
	TypedProperties properties();

	@NotNull
	AspectRepository aspectRepository();

	@NotNull
	WiredTypesConfiguration configuration();

	<T> void announce(@NotNull final T o);

	<T> void announce(@NotNull final IdentifiableProvider<T> identifiableProvider);

	@Nullable <T> Optional<T> tryGet(Class<T> type);

	/**
	 * Returns the instance of the provided type.
	 * <p>
	 * If the relevant provider is not found, the provider produces null,
	 * or the provided instance of the provider set for the type does
	 * not match the expected type, a {@link DiInstantiationException}
	 * will be thrown.
	 * <p>
	 * If multiple {@link IdentifiableProvider} instances are found for
	 * the requested type, it will be resolved using the set
	 * {@link WireConflictResolver}, which by default will be set according
	 * to {@link WireConflictStrategy}.
	 *
	 * @param type The type class of instance you want to have created
	 * @param <T> The generic type, matching the provided type class
	 * @return An instance according to the relevant, set {@link IdentifiableProvider}
	 * @throws DiInstantiationException if no provider is found, the provider
	 *                                  returns null, the set {@link WireConflictResolver}
	 *                                  throws the Exception or the supplied type of the
     *                                  provider does not match the expected type.
	 */
	@NotNull <T> T get(Class<T> type) throws DiInstantiationException;

	@NotNull <T> T get(@NotNull TypeIdentifier<T> identifier) throws DiInstantiationException;

	/**
	 *
	 * @param type
	 * @param <T>
	 * @return
	 */
	<T> List<T> getAll(Class<T> type);

	@NotNull <T> Stream<IdentifiableProvider<T>> stream(@NotNull Class<T> type);

	<T> Provider<T> getProvider(Class<T> type);

	Timed load();
}
