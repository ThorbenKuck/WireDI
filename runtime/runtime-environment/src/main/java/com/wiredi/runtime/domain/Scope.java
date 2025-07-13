package com.wiredi.runtime.domain;

import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.domain.factories.Bean;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.QualifiedTypeIdentifier;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.domain.scopes.CompositeScope;
import com.wiredi.runtime.domain.scopes.PrototypeScope;
import com.wiredi.runtime.domain.scopes.SingletonScope;
import com.wiredi.runtime.domain.scopes.ThreadLocalScope;
import com.wiredi.runtime.lang.ThrowingConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface Scope {

    @NotNull
    static ThreadLocalScope threadLocal() {
        return new ThreadLocalScope();
    }

    @NotNull
    static SingletonScope singleton() {
        return new SingletonScope();
    }

    @NotNull
    static SingletonScope threadSafeSingleton() {
        return SingletonScope.threadSafe();
    }

    @NotNull
    static PrototypeScope prototype() {
        return new PrototypeScope();
    }

    @NotNull
    static CompositeScope composite(@NotNull Scope... scopes) {
        return new CompositeScope(Arrays.asList(scopes));
    }

    @NotNull
    static CompositeScope composite(@NotNull Collection<Scope> scopes) {
        return new CompositeScope(scopes);
    }

    @NotNull <T> Optional<T> tryGet(@NotNull QualifiedTypeIdentifier<T> qualifierType);

    @NotNull <T> Optional<T> tryGet(@NotNull TypeIdentifier<T> qualifierType);

    <T> @NotNull IdentifiableProvider<T> getProvider(@NotNull TypeIdentifier<T> qualifierType);

    <T> @NotNull IdentifiableProvider<T> getProvider(@NotNull QualifiedTypeIdentifier<T> qualifierType);

    @NotNull <T> T get(@NotNull TypeIdentifier<T> qualifierType);

    @NotNull <T> T get(@NotNull QualifiedTypeIdentifier<T> qualifierType);

    <T> @NotNull Stream<Bean<T>> getAllBeans(@NotNull TypeIdentifier<T> type);

    @NotNull <T> List<@NotNull T> getAll(@NotNull TypeIdentifier<T> type);

    boolean contains(@NotNull TypeIdentifier<?> type);

    boolean contains(@NotNull QualifiedTypeIdentifier<?> type);

    boolean canSupply(@NotNull QualifiedTypeIdentifier<?> type);

    boolean canSupply(@NotNull TypeIdentifier<?> type);

    void register(@NotNull IdentifiableProvider<?> provider);

    default <E extends Throwable> void run(ThrowingConsumer<Scope, E> runnable) throws E {
        try {
            start();
            runnable.accept(this);
        } finally {
            finish();
        }
    }

    boolean isActive();

    /**
     * A method to handle auto starts.
     * <p>
     * Auto starts happen either at application startup, or if the scope is added after the application is started, at registration.
     * It behaves the same as start, but is an automatic action.
     * <p>
     * When applied during registration, this is typically invoked after {@link #registered(ScopeRegistry)} is called.
     * When applied during startup, the invocation difference between this and {@link #registered(ScopeRegistry)} is bigger.
     */
    default void autostart() {
    }

    /**
     * Starts this scope.
     * <p>
     * This may set up relevant information for the scope.
     * For example, a thread local scope may set the thread state in this method.
     */
    void start();

    /**
     * Finishes the scope.
     * <p>
     * This may clear the state or do other logic after the scope is concluded.
     * For example, a thread local scope may remove the thread state in this method.
     */
    void finish();

    /**
     * Notifies that this scope was registered at a ScopeRegistry.
     * <p>
     * This method is invoked before {@link #link(WireContainer)} is invoked and serves only to notify.
     * <p>
     * If the Scope is registered on an unlinked ScopeRegistry, it is
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
     * <p>
     * Repeated calls to link should throw an exception, as any Scope instance can only be linked to one {@link WireContainer}.
     * Only after {@link #unlink()} was called, should another call to this method succeed.
     *
     * @param wireRepository the repository to use for resolving data.
     */
    void link(WireContainer wireRepository);

    /**
     * Unlinks any associations and allows this Scope to be linked to other {@link WireContainer}.
     */
    void unlink();
}
