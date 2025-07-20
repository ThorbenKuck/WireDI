package com.wiredi.runtime;

import com.wiredi.runtime.domain.ScopeRegistry;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builder for creating and configuring {@link WireContainer} instances.
 * <p>
 * This builder provides a fluent API for configuring a WireContext before creating it.
 * It allows setting various configuration options like the environment and load configuration.
 * <p>
 * Example usage:
 * <pre>
 * WireContext context = WireContextBuilder.create()
 *     .withEnvironment(customEnvironment)
 *     .withInitializeEagerBeans(true)
 *     .withSynchronizeOnStates(true)
 *     .withStateFullMaxTimeout(Duration.ofSeconds(10))
 *     .build();
 * </pre>
 */
public class WireContainerBuilder {

    @NotNull
    private Environment environment;
    @NotNull
    private StartupDiagnostics startupDiagnostics = new StartupDiagnostics();
    @NotNull
    private ScopeRegistry scopeRegistry = new ScopeRegistry();
    @NotNull
    private WireContainerInitializer initializer = WireContainerInitializer.preconfigured();

    private final List<IdentifiableProvider<?>> providers = new ArrayList<>();

    private WireContainerBuilder(@NotNull Environment environment) {
        this.environment = environment;
    }

    /**
     * Creates a new builder with a default environment.
     *
     * @return a new builder instance
     */
    @NotNull
    public static WireContainerBuilder create() {
        return new WireContainerBuilder(Environment.build());
    }

    /**
     * Creates a new builder with the specified environment.
     *
     * @param environment the environment to use
     * @return a new builder instance
     */
    @NotNull
    public static WireContainerBuilder create(@NotNull Environment environment) {
        return new WireContainerBuilder(environment);
    }

    /**
     * Sets the environment to use for the WireContext.
     *
     * @param environment the environment to use
     * @return this builder instance
     */
    @NotNull
    public WireContainerBuilder withEnvironment(@NotNull Environment environment) {
        this.environment = environment;
        return this;
    }

    /**
     * Adds a provider to the list of providers that will be registered with the WireContainer.
     *
     * @param provider the provider to add
     * @return this builder instance
     */
    @NotNull
    public WireContainerBuilder withProvider(@NotNull IdentifiableProvider<?> provider) {
        this.providers.add(provider);
        return this;
    }

    /**
     * Adds multiple providers to the list of providers that will be registered with the WireContainer.
     *
     * @param providers the list of providers to add
     * @return this builder instance
     */
    @NotNull
    public WireContainerBuilder withProviders(@NotNull List<IdentifiableProvider<?>> providers) {
        this.providers.addAll(providers);
        return this;
    }

    /**
     * Adds multiple providers to the list of providers that will be registered with the WireContainer.
     *
     * @param providers the providers to add
     * @return this builder instance
     */
    @NotNull
    public WireContainerBuilder withProviders(@NotNull IdentifiableProvider<?>... providers) {
        this.providers.addAll(Arrays.asList(providers));
        return this;
    }

    /**
     * Sets the startup diagnostics to use.
     *
     * @param startupDiagnostics the startup diagnostics to use
     * @return this builder instance
     */
    @NotNull
    public WireContainerBuilder withStartupDiagnostics(@NotNull StartupDiagnostics startupDiagnostics) {
        this.startupDiagnostics = startupDiagnostics;
        return this;
    }

    /**
     * Sets the scope registry to use.
     *
     * @param scopeRegistry the scope registry to use
     * @return this builder instance
     */
    @NotNull
    public WireContainerBuilder withScopeRegistry(@NotNull ScopeRegistry scopeRegistry) {
        this.scopeRegistry = scopeRegistry;
        return this;
    }

    /**
     * Sets the container initializer to use.
     *
     * @param initializer the container initializer to use
     * @return this builder instance
     */
    @NotNull
    public WireContainerBuilder withInitializer(@NotNull WireContainerInitializer initializer) {
        this.initializer = initializer;
        return this;
    }

    /**
     * Builds a new WireContext with the configured options.
     * The WireContext is not loaded.
     *
     * @return a new, unloaded WireContext
     */
    @NotNull
    public WireContainer build() {
        return new WireContainer(
                environment,
                startupDiagnostics,
                scopeRegistry,
                initializer
        );
    }

    /**
     * Builds a new WireContainer with the configured options and applies a post-processor.
     * The WireContainer is not loaded.
     *
     * @param postProcessor a consumer that can perform additional configuration on the built container
     * @return a new, unloaded WireContainer
     */
    @NotNull
    public WireContainer build(Consumer<WireContainer> postProcessor) {
        WireContainer container = build();
        postProcessor.accept(container);
        return container;
    }

    /**
     * Builds a new WireContainer, applies the preLoadProcessor, and then loads it.
     * 
     * @param preLoadProcessor a consumer that can configure the WireContainer before loading
     * @return a new, loaded WireContainer
     */
    @NotNull
    public WireContainer load(Consumer<WireContainer> preLoadProcessor) {
        WireContainer container = build();
        preLoadProcessor.accept(container);
        container.load();
        return container;
    }

    /**
     * Builds a new WireContainer and loads it.
     * 
     * @return a new, loaded WireContainer
     */
    @NotNull
    public WireContainer load() {
        WireContainer container = build();
        container.load();
        return container;
    }
}
