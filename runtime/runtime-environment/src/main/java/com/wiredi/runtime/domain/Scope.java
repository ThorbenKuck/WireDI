package com.wiredi.runtime.domain;

import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.QualifiedTypeIdentifier;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.domain.scopes.CompositeScope;
import com.wiredi.runtime.domain.scopes.PrototypeScope;
import com.wiredi.runtime.domain.scopes.SingletonScope;
import com.wiredi.runtime.domain.scopes.ThreadLocalScope;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public interface Scope {

    @NotNull
    static Scope threadLocal() {
        return new ThreadLocalScope();
    }

    @NotNull
    static Scope singleton() {
        return new SingletonScope();
    }

    @NotNull
    static Scope prototype() {
        return new PrototypeScope();
    }

    @NotNull
    static Scope composite(@NotNull Scope... scopes) {
        return new CompositeScope(Arrays.asList(scopes));
    }

    @NotNull
    static Scope composite(@NotNull Collection<Scope> scopes) {
        return new CompositeScope(scopes);
    }

    @NotNull <T> Optional<T> tryGet(@NotNull QualifiedTypeIdentifier<T> qualifierType);

    @NotNull <T> T get(@NotNull QualifiedTypeIdentifier<T> qualifierType);

    boolean contains(@NotNull QualifiedTypeIdentifier<?> type);

    @NotNull <T> Collection<@NotNull T> getAll(@NotNull TypeIdentifier<T> type);

    void register(@NotNull IdentifiableProvider<?> provider);

    boolean canSupply(@NotNull QualifiedTypeIdentifier<?> type);

    /**
     * Starts this scope.
     * <p>
     * This may set up relevant information for the scope.
     * For example, a thread local scope may set the thread state in this method.
     */
    default void start() {
    }

    /**
     * Finishes the scope.
     * <p>
     * This may clear the state or do other logic after the scope is concluded.
     * For example, a thread local scope may remove the thread state in this method.
     */
    void finish();

    /**
     * Notifies that this scope was registered at a ScopeRegistry.
     *
     * @param registry the registry it was registered at.
     */
    default void registered(ScopeRegistry registry) {
    }

    /**
     * Notifies that this scope was unregistered from a ScopeRegistry.
     *
     * @param registry the registry it was unregistered from.
     */
    default void unregistered(ScopeRegistry registry) {
    }

    /**
     * Links the Scope with a WireRepository.
     * <p>
     * This is commonly done when the Scope is registered at a ScopeRegistry, before the {@link #registered(ScopeRegistry)} method is invoked.
     * The method is different to make testing possible.
     *
     * @param wireRepository the repository to use for resolving data.
     */
    void link(WireRepository wireRepository);

}
