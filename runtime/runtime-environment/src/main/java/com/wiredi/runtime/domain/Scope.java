package com.wiredi.runtime.domain;

import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.domain.factories.Bean;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.QualifiedTypeIdentifier;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.domain.scopes.PrototypeScope;
import com.wiredi.runtime.domain.scopes.SingletonScope;
import com.wiredi.runtime.domain.scopes.ThreadLocalScope;
import com.wiredi.runtime.domain.scopes.UnionScope;
import com.wiredi.runtime.lang.Ordered;
import com.wiredi.runtime.lang.ThrowingConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A scope is a container for bean instances.
 * <p>
 * Each scope can have its own lifecycle, defining when instances are created and destroyed.
 * Common scopes include {@link SingletonScope}, {@link PrototypeScope}, and {@link ThreadLocalScope}.
 * <p>
 * Scopes itself are only containers that can hold instances.
 * To make a scope behave the way It's supposed to, it is necessary to control it.
 * Because scopes are just containers, it does not matter which implementation of a scope you have, you have to
 * manually open and close it.
 * Opening and Closing a scope is the easiest way of using a scope, but keep in mind that scopes can also be used concurrently.
 * <p>
 * Another way is to write a scope which is aware of the concurrency.
 * One such scope is the {@link ThreadLocalScope}, which holds instances in a thread local variable.
 * For example, when implementing a request scope, you can use a ThreadLocalScope to hold the request data.
 * Imagine that you have set up a request scope in your application for an annotation called {@code @Request} and a correlating scope provider.
 * To now use this scope, you can open it before handling a request and close it afterward:
 *
 * <pre>{@code
 * class RequestScopeExample {
 *     private final ScopeRegistry registry;
 *
 *     public RequestScopeExample(ScopeRegistry registry) {
 *         this.registry = registry;
 *     }
 *
 *     public void handleRequest(Runnable runnable) {
 *         Scope scope = registry.scope(Request.class); // Our custom scope annotation
 *         scope.open();
 *         try {
 *             runnable.run();
 *         } finally {
 *             scope.close();
 *         }
 *     }
 * }
 * }</pre>
 * <p>
 * It is important to note that this example only works because it uses a thread local scope.
 * <p>
 * Scopes are ultimately used when a bean is requested from the {@link WireContainer}.
 * The {@link WireContainer} will first ask the {@link ScopeRegistry} to determine which scope is responsible for a Bean.
 * If a scope could be determined, it will ask the scope for a bean instance.
 * The scope is then responsible for managing the lifecycle of the bean instance, creating and optionally caching it.
 * Commonly, the scope will use a {@link com.wiredi.runtime.domain.scopes.cache.ScopeStore} to cache bean instances.
 * <p>
 * It is highly recommended to use the {@link com.wiredi.runtime.domain.scopes.AbstractScope}, as it provides a lot of
 * useful functionality plus implementations for common scopes.
 *
 * @see WireContainer#scopeRegistry()
 * @see com.wiredi.runtime.domain.scopes.cache.ScopeStore
 * @see com.wiredi.runtime.domain.scopes.AbstractScope
 * @see SingletonScope
 * @see PrototypeScope
 * @see ThreadLocalScope
 */
public interface Scope extends Ordered {

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
    static UnionScope union(@NotNull Scope... scopes) {
        return new UnionScope(Arrays.asList(scopes));
    }

    @NotNull
    static UnionScope union(@NotNull Collection<Scope> scopes) {
        return new UnionScope(scopes);
    }

    @NotNull <T> Optional<T> tryGet(@NotNull QualifiedTypeIdentifier<T> qualifierType);

    @NotNull <T> Optional<T> tryGet(@NotNull TypeIdentifier<T> qualifierType);

    <T> @NotNull IdentifiableProvider<T> getProvider(@NotNull TypeIdentifier<T> qualifierType);

    <T> @NotNull IdentifiableProvider<T> getProvider(@NotNull QualifiedTypeIdentifier<T> qualifierType);

    @NotNull <T> T get(@NotNull TypeIdentifier<T> qualifierType);

    @NotNull <T> T get(@NotNull QualifiedTypeIdentifier<T> qualifierType);

    <T> @NotNull Stream<Bean<T>> getAllBeans(@NotNull TypeIdentifier<T> type);

    @NotNull <T> List<@NotNull T> getAll(@NotNull TypeIdentifier<T> type);

    boolean contains(@NotNull QualifiedTypeIdentifier<?> type);

    boolean contains(@NotNull TypeIdentifier<?> type);

    boolean canSupply(@NotNull QualifiedTypeIdentifier<?> type);

    boolean canSupply(@NotNull TypeIdentifier<?> type);

    /**
     * Registers a provider with this scope.
     * <p>
     * When invoked, the Scope is expected to create a factory for the provider and store it.
     * However, this is done at the discretion of the Scope implementation.
     * Commonly, the Scopes will use a {@link BeanFactory} to wrap the provider.
     *
     * @param provider the provider to register.
     */
    void register(@NotNull IdentifiableProvider<?> provider);

    /**
     * Uses the scope to run the provided runnable.
     * <p>
     * The scope will be asked to start before and reset itself after the runnable is executed.
     * If the Scope is not thread safe, it is recommended to use a thread safe scope or synchronize the execution.
     *
     * @param runnable the runnable to execute.
     * @param <E>      the type of exception that can be thrown by the runnable.
     * @throws E the exception that can be thrown by the runnable.
     */
    default <E extends Throwable> void run(ThrowingConsumer<Scope, E> runnable) throws E {
        try {
            start();
            runnable.accept(this);
        } finally {
            reset();
        }
    }

    /**
     * A method for what to do when the scope is opened.
     * <p>
     * By default, this will reset the scope to its initial state.
     * Implementations may override this method to perform additional actions.
     */
    default void start() {
        reset();
    }

    /**
     * Closes the scope.
     * <p>
     * This method is called when the scope is finished.
     * With this method, the scope can clean up any resources it might have used and removed preserved beans.
     * These beans will be destroyed.
     */
    void reset();

    /**
     * Notifies that this scope was registered at a ScopeRegistry.
     * <p>
     * This method is invoked before {@link #link(WireContainer)} is invoked and serves only to notify.
     * <p>
     * If the Scope is registered on an unlinked ScopeRegistry, it is
     *
     * @param registry the registry it was registered at.
     */
    default void registered(@NotNull ScopeRegistry registry) {
    }

    /**
     * Notifies that this scope was unregistered from a ScopeRegistry.
     *
     * @param registry the registry it was unregistered from.
     */
    default void unregistered(@NotNull ScopeRegistry registry) {
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
     * @param wireContainer the repository to use for resolving data.
     */
    void link(@NotNull WireContainer wireContainer);

    /**
     * Sets the callback to use for this scope.
     * <p>
     * The callback is stored and invoked on this scope in its lifecycle.
     *
     * @param scopeCallback the callback to use.
     * @see ScopeCallback
     */
    void callback(@NotNull ScopeCallback scopeCallback);

    /**
     * Unlinks any associations and allows this Scope to be linked to other {@link WireContainer}.
     */
    void unlink();

}
