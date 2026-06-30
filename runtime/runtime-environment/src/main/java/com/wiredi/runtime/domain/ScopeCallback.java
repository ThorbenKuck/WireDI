package com.wiredi.runtime.domain;

import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.domain.factories.Bean;
import com.wiredi.runtime.lang.Ordered;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * A callback for scope events.
 * <p>
 * The ScopeCallback is invoked when a scope is starting, closing, or a bean is created.
 * It can also be used to modify created beans, which effectively means you can replace beans as they are created.
 * See {@link #newBeanCreated(Bean)} for more details.
 * <p>
 * This callback has a special lifecycle, different from the lifecycle of other beans in the container.
 * Because it needs to be invoked in the Scopes which are creating beans, a ScopeCallback is requested eagerly by the
 * {@link ScopeRegistry} from all available scopes.
 * It does not go through the normal bean lifecycle.
 * <p>
 * Additionally, this also means that dependencies of a ScopeCallback are to be used carefully.
 * Dependencies will be resolved as normal, but they are resolved eagerly and before any other beans are initiated.
 * This means that an eager dependency will be constructed after the ScopeCallback dependencies have been constructed.
 * A dependency of the ScopeCallback that is eager can be used before the {@link Eager#initialize(WireContainer)}
 * method is called.
 * <p>
 * The most common usecase might be, to inject the {@link WireContainer} into a ScopeCallback.
 * This container can be used to resolve dependencies of the ScopeCallback lazily.
 * However, still this is something to be very careful with.
 *
 * @see Scope
 */
public interface ScopeCallback extends Ordered {
    @NotNull ScopeCallback NOOP = new Noop();

    /**
     * Invoked when a bean is created.
     * <p>
     * The passed {@link Bean} is the bean that has been created.
     * It is always guaranteed to be a completely new Bean instance.
     * <p>
     * This method can be used to modify the created Bean.
     * If needed, you can return a different Bean instance than the one passed into the method.
     * The returned Bean will be used in further processing, meaning that if a different bean is returned, the original
     * will not be used anymore.
     * <p>
     * ### Important
     * <p>
     * The returned value is not validated.
     * You can replace a Bean with a Bean of a different type, which will lead to unexpected behavior.
     * Be very careful when using this method.
     *
     * @param bean the created bean
     * @param <T>  the generic value type of the bean
     * @return a bean to use in the scope
     */
    @NotNull
    default <T> Bean<T> newBeanCreated(@NotNull Bean<T> bean) {
        return bean;
    }

    /**
     * Invoked when a scope is closing.
     *
     * @param scope the scope that is closing
     */
    default void scopeResetting(Scope scope) {
    }

    /**
     * Invoked when a scope has successfully closed.
     *
     * @param scope the scope that has closed
     */
    default void scopeReset(Scope scope) {
    }

    /**
     * A noop implementation of ScopeCallback.
     * <p>
     * This implementation does not do anything.
     * It can be used as a not-null placeholder instance or as a stand-in for non-null fields if no other
     * implementation is available.
     */
    class Noop implements ScopeCallback {
    }

    class Composite implements ScopeCallback {
        private final Collection<ScopeCallback> callbacks;

        public Composite(Collection<ScopeCallback> callbacks) {
            this.callbacks = Ordered.ordered(callbacks);
        }

        @Override
        public @NotNull <T> Bean<T> newBeanCreated(@NotNull Bean<T> bean) {
            Bean<T> result = bean;
            for (ScopeCallback callback : callbacks) {
                result = callback.newBeanCreated(result);
            }
            return result;
        }

        @Override
        public void scopeReset(Scope scope) {
            for (ScopeCallback callback : callbacks) {
                callback.scopeReset(scope);
            }
        }

        @Override
        public void scopeResetting(Scope scope) {
            for (ScopeCallback callback : callbacks) {
                callback.scopeResetting(scope);
            }
        }
    }
}
