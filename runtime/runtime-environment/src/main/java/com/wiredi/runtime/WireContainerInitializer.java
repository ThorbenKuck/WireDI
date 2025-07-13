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
import com.wiredi.runtime.exceptions.DiInstantiationException;
import com.wiredi.runtime.exceptions.DiLoadingException;
import com.wiredi.runtime.lang.Counter;
import com.wiredi.runtime.lang.OrderedComparator;
import com.wiredi.runtime.time.Timed;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public class WireContainerInitializer {

    @NotNull
    private static final Logging logger = Logging.getInstance(WireContainerInitializer.class);
    @NotNull
    private static final String ROUND_LOGGING_PREFIX = "Applied %s conditional providers in %s rounds.";
    @NotNull
    private static final String ROUND_WARNING_SUFFIX = " Consider to optimize the condition orders to reduce the rounds required for conditional checks.";
    @NotNull
    private final List<@NotNull IdentifiableProviderSource> sources = new ArrayList<>();

    public WireContainerInitializer(IdentifiableProviderSource... sources) {
        this.sources.addAll(Arrays.asList(sources));
    }

    public WireContainerInitializer(List<? extends IdentifiableProviderSource> sources) {
        this.sources.addAll(sources);
    }

    public static WireContainerInitializer preconfigured() {
        return new WireContainerInitializer(new ServiceLoaderIdentifiableProviderSource());
    }

    public void addSource(@NotNull IdentifiableProviderSource source) {
        this.sources.add(source);
    }

    public void removeSource(@NotNull IdentifiableProviderSource source) {
        this.sources.remove(source);
    }

    public void addSources(@NotNull List<? extends IdentifiableProviderSource> sources) {
        this.sources.addAll(sources);
    }

    public void removeSources(@NotNull List<? extends IdentifiableProviderSource> sources) {
        this.sources.removeAll(sources);
    }

    public void setSources(@NotNull IdentifiableProviderSource... sources) {
        this.setSources(Arrays.asList(sources));
    }

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
     * Resolves the scope of a {@link IdentifiableProvider} for conditional providers.
     * For immediate registration, use ScopeRegistry.registerProvider() instead.
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

    private void printDebugInfo(ConditionEvaluationContext context) {
        Collection<ConditionEvaluationReporter> reporters = context.wireContainer().getAll(ConditionEvaluationReporter.class);
        if (reporters.isEmpty()) {
            ConditionEvaluationReporter.SYSTEM_OUT.report(context);
        } else {
            reporters.forEach(reporter -> reporter.report(context));
        }
    }

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