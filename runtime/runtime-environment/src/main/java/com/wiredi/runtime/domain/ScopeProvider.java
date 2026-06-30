package com.wiredi.runtime.domain;

import com.wiredi.runtime.domain.scopes.JoinedScopeProvider;
import com.wiredi.runtime.domain.scopes.SimpleScopeProvider;
import com.wiredi.runtime.domain.scopes.provider.AnnotationBasedScopeProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;

/**
 * A provider to get a {@link Scope}.
 * <p>
 * This class is used by the {@link com.wiredi.runtime.domain.provider.IdentifiableProvider} to determine its scope.
 * Each {@link com.wiredi.runtime.domain.provider.IdentifiableProvider} can have a custom scope its instances are maintained in.
 * And this provider is responsible for constructing the scope if it is not already present in the {@link ScopeRegistry}.
 * <p>
 * With this inversion of responsibility, we have the possibility to dynamically create new scopes and generate code that
 * constructs custom scopes.
 * <p>
 * The annotation processors use the AnnotationBaseScopeProvider.
 * If an annotation, meta annotated with {@link jakarta.inject.Scope} is present on a class annotated with
 * {@link com.wiredi.annotations.Wire}, the generated provider will use a {@link AnnotationBasedScopeProvider}.
 * The first provider to be loaded with this provider will add the scope to the {@link ScopeRegistry}.
 * Any later provider will simply return the same scope from the {@link ScopeRegistry}.
 * <p>
 * You can also provide custom providers in {@link com.wiredi.runtime.domain.provider.IdentifiableProvider} to provide
 * custom scopes if you want.
 */
public interface ScopeProvider {

    /**
     * Create a new Builder based on an annotation.
     *
     * @param type the annotation type for which a builder should be created
     * @return a new builder
     * @param <T> the annotation type
     */
    static <T extends Annotation> AnnotationBasedScopeProvider.Builder<T> forAnnotation(Class<T> type) {
        return new AnnotationBasedScopeProvider.Builder<>(type);
    }

    /**
     * Create a new Builder based on an identifier.
     *
     * @param identifier the identifier for which a builder should be created
     * @return a new builder
     */
    static SimpleScopeProvider.Builder forIdentifier(Object identifier) {
        return new SimpleScopeProvider.Builder(identifier);
    }

    /**
     * Get the scope for this provider.
     * <p>
     * This method should use the provided {@link ScopeRegistry} to construct the scope if it is not already present.
     * Then the scope can be reused by other providers and will be maintained by the {@link ScopeRegistry},
     * which itself is maintained by the lifecycle of the WireContainer.
     * <p>
     * If you do not use the {@link ScopeRegistry}, you will have to manage the lifecycle of the scope yourself.
     * If the scope is not present in the ScopeRegistry, it will not be asked to return instances from the WireContainer.
     * <p>
     * @param registry the registry of the current WireContainer
     * @return the scope for this provider, or null if the scope could not be constructed.
     */
    @Nullable
    Scope getScope(@NotNull ScopeRegistry registry);

    /**
     * Adds this provider to another provider.
     * <p>
     * This method is used to combine multiple scope providers into one.
     * <p>
     * @param provider the provider to combine with this provider
     * @return a new provider that combines this provider with the given provider
     * @see JoinedScopeProvider
     */
    @NotNull
    default ScopeProvider and(@NotNull ScopeProvider provider) {
        return new JoinedScopeProvider(this, provider);
    }
}
