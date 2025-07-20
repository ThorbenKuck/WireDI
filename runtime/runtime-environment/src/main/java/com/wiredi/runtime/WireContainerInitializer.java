package com.wiredi.runtime;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.domain.Scope;
import com.wiredi.runtime.domain.ScopeProvider;
import com.wiredi.runtime.domain.ScopeRegistry;
import com.wiredi.runtime.domain.conditional.ConditionEvaluation;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.IdentifiableProviderSource;
import com.wiredi.runtime.domain.provider.condition.LoadCondition;
import com.wiredi.runtime.domain.provider.sources.ServiceLoaderIdentifiableProviderSource;
import com.wiredi.runtime.lang.Counter;
import com.wiredi.runtime.lang.OrderedComparator;
import com.wiredi.runtime.time.Timed;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

/**
 * The WireContainerInitializer is responsible for initializing a WireContainer by loading
 * and configuring providers from various sources.
 * <p>
 * This class manages the process of:
 * <ul>
 *   <li>Loading providers from configured sources</li>
 *   <li>Resolving the appropriate scope for each provider</li>
 *   <li>Applying conditional logic to determine which providers should be included</li>
 *   <li>Registering providers with the WireContainer</li>
 * </ul>
 * <p>
 * The initializer works with {@link IdentifiableProviderSource} instances to obtain
 * providers that will be registered with the container.
 * 
 * @see WireContainer
 * @see IdentifiableProviderSource
 * @see IdentifiableProvider
 */
public class WireContainerInitializer {

    @NotNull
    private static final Logging logger = Logging.getInstance(WireContainerInitializer.class);
    @NotNull
    private static final String ROUND_LOGGING_PREFIX = "Applied %s conditional providers in %s rounds.";
    @NotNull
    private static final String ROUND_WARNING_SUFFIX = " Consider to optimize the condition orders to reduce the rounds required for conditional checks.";
    @NotNull
    private final List<@NotNull IdentifiableProviderSource> sources = new ArrayList<>();

    /**
     * Creates a new WireContainerInitializer with the specified provider sources.
     *
     * @param sources the provider sources to use
     */
    public WireContainerInitializer(IdentifiableProviderSource... sources) {
        this.sources.addAll(Arrays.asList(sources));
    }

    /**
     * Creates a new WireContainerInitializer with the specified provider sources.
     *
     * @param sources the list of provider sources to use
     */
    public WireContainerInitializer(List<? extends IdentifiableProviderSource> sources) {
        this.sources.addAll(sources);
    }

    /**
     * Creates a new WireContainerInitializer with a default ServiceLoaderIdentifiableProviderSource.
     * <p>
     * This is the recommended way to create a WireContainerInitializer for most use cases.
     *
     * @return a new preconfigured WireContainerInitializer
     */
    public static WireContainerInitializer preconfigured() {
        return new WireContainerInitializer(new ServiceLoaderIdentifiableProviderSource());
    }

    /**
     * Adds a provider source to the initializer.
     *
     * @param source the provider source to add
     */
    public void addSource(@NotNull IdentifiableProviderSource source) {
        this.sources.add(source);
    }

    /**
     * Removes a provider source from the initializer.
     *
     * @param source the provider source to remove
     */
    public void removeSource(@NotNull IdentifiableProviderSource source) {
        this.sources.remove(source);
    }

    /**
     * Adds multiple provider sources to the initializer.
     *
     * @param sources the list of provider sources to add
     */
    public void addSources(@NotNull List<? extends IdentifiableProviderSource> sources) {
        this.sources.addAll(sources);
    }

    /**
     * Removes multiple provider sources from the initializer.
     *
     * @param sources the list of provider sources to remove
     */
    public void removeSources(@NotNull List<? extends IdentifiableProviderSource> sources) {
        this.sources.removeAll(sources);
    }

    /**
     * Replaces all current provider sources with the specified ones.
     *
     * @param sources the provider sources to set
     */
    public void setSources(@NotNull IdentifiableProviderSource... sources) {
        this.setSources(Arrays.asList(sources));
    }

    /**
     * Replaces all current provider sources with the specified ones.
     *
     * @param sources the list of provider sources to set
     */
    public void setSources(@NotNull List<? extends IdentifiableProviderSource> sources) {
        this.sources.clear();
        this.sources.addAll(sources);
    }

    /**
     * Loads all available {@link IdentifiableProvider}.
     * <p>
     * This method uses the {@link ServiceFiles} to load providers.
     *
     * @return the time it took to load the BeanContainer
     */
    @NotNull
    public Timed initialize(@NotNull WireContainer wireContainer) {
        if (this.sources.isEmpty()) {
            throw new IllegalStateException("A WireContainer cannot be initialized without any sources.");
        }
        // pre-check to avoid unnecessary synchronization
        StartupDiagnostics startupDiagnostics = wireContainer.startupDiagnostics();

        ProviderCatalog providerCatalog = new ProviderCatalog();
        logger.debug("Registering all known identifiable providers");
        Timed timed = startupDiagnostics.measure("WireBootstrap.load", () -> {
            Stream<IdentifiableProvider<?>> providers = startupDiagnostics.measure("WireBootstrap.loadProviders", () -> this.loadProviders(startupDiagnostics))
                    .then(timedValue -> logger.debug(() -> "Loaded IdentifiableProviders in " + timedValue.time()))
                    .value();
            startupDiagnostics.measure("WireBootstrap.constructProviderCatalog", () -> fillProviderCatalog(wireContainer, providerCatalog, providers))
                    .then(t -> logger.debug(() -> "Constructed provider catalog in " + t));

            if (providerCatalog.hasErrors()) {
                throw providerCatalog.printErrors();
            }

            startupDiagnostics.measure("WireBootstrap.applyConditionals", () -> applyConditionals(wireContainer, providerCatalog))
                    .then(t -> logger.debug("Applied conditionals in " + t));
        });
        logger.debug(() -> "Registered " + providerCatalog.countRegisteredProviders() + "identifiable providers in " + timed);
        return timed;
    }

    /**
     * Loads all IdentifiableProviders from all sources into a stream.
     * <p>
     * The Stream returned by this method is used to then load scopes and apply conditional providers.
     *
     * @param startupDiagnostics the diagnostics, to register the time it took to load.
     * @return a standalone stream that can be analyzed.
     */
    private Stream<IdentifiableProvider<?>> loadProviders(StartupDiagnostics startupDiagnostics) {
        return startupDiagnostics.measure("WireBootstrap.loadProviders", () -> sources.stream().flatMap(source -> source.load().stream()))
                .then(timedValue -> logger.debug(() -> "Loaded IdentifiableProviders in " + timedValue.time()))
                .value();
    }

    /**
     * Fills the provider catalog with providers from the stream.
     * <p>
     * This method processes each provider in the stream and either:
     * <ul>
     *   <li>Adds it to the conditional providers list if it has a condition</li>
     *   <li>Registers it directly with the scope registry if it has no condition</li>
     * </ul>
     * Any errors during registration are captured in the provider catalog.
     *
     * @param wireContainer    the wire container to use for registration
     * @param providerCatalog  the catalog to fill with providers
     * @param providerStream   the stream of providers to process
     */
    private void fillProviderCatalog(
            @NotNull WireContainer wireContainer,
            @NotNull ProviderCatalog providerCatalog,
            @NotNull Stream<IdentifiableProvider<?>> providerStream
    ) {
        ScopeRegistry scopeRegistry = wireContainer.scopeRegistry();

        providerStream.forEach(provider -> {
            LoadCondition condition = provider.condition();
            if (condition != null) {
                // For conditional providers, we still need to determine the scope but defer registration
                Scope scope = resolveScope(scopeRegistry, provider);
                providerCatalog.addConditionalProvider(provider, scope);
            } else {
                logger.trace(() -> "Registering instance of type " + provider.getClass() + " with wired types " + provider.additionalWireTypes() + " and qualifiers " + provider.qualifiers());
                try {
                    // Use ScopeRegistry's smart registration instead of manual scope resolution
                    scopeRegistry.registerProvider(provider);
                    providerCatalog.addSuccessfullyRegisteredProvider(provider);
                } catch (@NotNull Throwable throwable) {
                    logger.error(() -> "Failed to register provider " + provider.getClass().getSimpleName(), throwable);
                    providerCatalog.noteError(provider, throwable);
                }
            }
        });

        scopeRegistry.initialize(wireContainer);
    }

    /**
     * Resolves the appropriate scope for a provider.
     * <p>
     * This method determines which scope a provider should be registered in by:
     * <ol>
     *   <li>Checking if the provider has a specified scope</li>
     *   <li>Using that scope if available</li>
     *   <li>Falling back to the default scope if no scope is specified or if the specified scope is not available</li>
     * </ol>
     * <p>
     * Note: For immediate registration, use ScopeRegistry.registerProvider() instead.
     * This method is primarily used for conditional providers that need scope resolution
     * before their conditions are evaluated.
     *
     * @param scopeRegistry        the registry containing available scopes
     * @param identifiableProvider the provider to resolve the scope for
     * @return the resolved scope for the provider
     */
    @NotNull
    private Scope resolveScope(
            @NotNull ScopeRegistry scopeRegistry,
            @NotNull IdentifiableProvider<?> identifiableProvider
    ) {
        final ScopeProvider scopeProvider = identifiableProvider.scope();
        if (scopeProvider == null) {
            return scopeRegistry.getDefaultScope();
        }

        Scope scope = scopeProvider.getScope(scopeRegistry);
        return scope != null ? scope : scopeRegistry.getDefaultScope();
    }

    /**
     * Applies conditional providers to the wire container.
     * <p>
     * This method processes all conditional providers in the catalog by:
     * <ol>
     *   <li>Sorting them based on their order</li>
     *   <li>Evaluating their conditions</li>
     *   <li>Registering those whose conditions are met</li>
     * </ol>
     * <p>
     * The method may perform multiple rounds of condition evaluation as some conditions
     * may depend on beans that are registered by other conditional providers.
     *
     * @param wireContainer   the wire container to register providers with
     * @param providerCatalog the catalog containing conditional providers
     */
    private void applyConditionals(
            @NotNull WireContainer wireContainer,
            @NotNull ProviderCatalog providerCatalog
    ) {
        List<ProviderCatalog.ProviderScope> conditionalProviders = providerCatalog.conditionalProviders();
        if (conditionalProviders.isEmpty()) {
            return;
        }

        ConditionEvaluation conditionEvaluation = new ConditionEvaluation(wireContainer);
        Integer conditionalRoundThreshold = wireContainer.environment().getProperty(PropertyKeys.CONDITIONAL_ROUND_THRESHOLD.getKey(), 10);

        logger.debug(() -> "Detected " + conditionalProviders.size() + " conditional providers.");
        Counter additionalRounds = new Counter();

        // Sort once upfront for better condition resolution order
        List<ProviderCatalog.ProviderScope> sortedProviders = new ArrayList<>(OrderedComparator.sorted(conditionalProviders));
        Counter appliedConditionalProviders = applyConditionals(sortedProviders, additionalRounds, conditionEvaluation, providerCatalog);

        if (additionalRounds.get() > conditionalRoundThreshold) {
            logger.warn(() -> ROUND_LOGGING_PREFIX.formatted(appliedConditionalProviders.get(), additionalRounds.get()) + ROUND_WARNING_SUFFIX);
        } else {
            logger.debug(() -> ROUND_LOGGING_PREFIX.formatted(appliedConditionalProviders.get(), additionalRounds.get()));
        }

        // Only do debug output if debug is enabled (avoid property lookup in hot path)
        if (wireContainer.environment().debugEnabled()) {
            printDebugInfo(new ConditionEvaluationContext(providerCatalog, appliedConditionalProviders, additionalRounds, conditionalRoundThreshold, conditionEvaluation, wireContainer));
        }
    }

    /**
     * Prints debug information about condition evaluation.
     * <p>
     * This method uses ConditionEvaluationReporter instances to report information about
     * the condition evaluation process. If no reporters are found in the container,
     * it falls back to using the SYSTEM_OUT reporter.
     *
     * @param context the context containing information about the condition evaluation
     */
    private void printDebugInfo(ConditionEvaluationContext context) {
        Collection<ConditionEvaluationReporter> reporters = context.wireContainer().getAll(ConditionEvaluationReporter.class);
        if (reporters.isEmpty()) {
            ConditionEvaluationReporter.SYSTEM_OUT.report(context);
        } else {
            reporters.forEach(reporter -> reporter.report(context));
        }
    }

    /**
     * Applies conditional providers in multiple rounds.
     * <p>
     * This method iteratively evaluates conditions for providers and registers those
     * whose conditions are met. It continues this process in rounds until either:
     * <ul>
     *   <li>All providers have been processed</li>
     *   <li>No more providers can be applied in a round</li>
     * </ul>
     * <p>
     * This multi-round approach allows for dependencies between conditional providers,
     * where one provider's registration might satisfy the condition for another provider.
     *
     * @param identifiableProviders the list of providers with their scopes to process
     * @param round                 a counter tracking the number of rounds
     * @param conditionEvaluation   the evaluation context for conditions
     * @param providerCatalog       the catalog to register successful providers with
     * @return a counter indicating how many providers were successfully applied
     */
    private Counter applyConditionals(
            @NotNull List<ProviderCatalog.ProviderScope> identifiableProviders,
            @NotNull Counter round,
            @NotNull ConditionEvaluation conditionEvaluation,
            @NotNull ProviderCatalog providerCatalog
    ) {
        Counter applied = new Counter();
        List<ProviderCatalog.ProviderScope> leftoverProviders = new ArrayList<>(OrderedComparator.sorted(identifiableProviders));
        List<ProviderCatalog.ProviderScope> currentNotMatched = new ArrayList<>(leftoverProviders.size());
        boolean anyApplied;
        logger.debug(() -> "Applying conditional providers; Round " + round.get());

        do {
            anyApplied = false;
            round.increment();
            currentNotMatched.clear(); // Reuse the list instead of creating new ones

            for (ProviderCatalog.ProviderScope providerScope : leftoverProviders) {
                IdentifiableProvider<?> provider = providerScope.provider();
                LoadCondition condition = Objects.requireNonNull(provider.condition());
                ConditionEvaluation.Context context = conditionEvaluation.access(provider);
                context.reset();
                condition.test(context);

                if (context.isMatched()) {
                    try {
                        providerScope.register();
                        providerCatalog.addSuccessfullyRegisteredProvider(provider);
                        applied.increment();
                        anyApplied = true;
                        logger.trace(() -> "Applied conditional provider " + provider.getClass().getSimpleName());
                    } catch (Throwable throwable) {
                        providerCatalog.noteError(provider, throwable);
                    }
                } else {
                    currentNotMatched.add(providerScope);
                }
            }

            logger.debug(() -> "Conditional Round " + round.get() + ". Totally Applied conditional providers: " + applied.get());
            // Swap references instead of clearing and adding all
            List<ProviderCatalog.ProviderScope> temp = leftoverProviders;
            leftoverProviders = currentNotMatched;
            currentNotMatched = temp;
        } while (anyApplied && !leftoverProviders.isEmpty());

        logger.debug(() -> "Applied " + applied.get() + " conditions in round " + round.get());
        return applied;
    }
}
