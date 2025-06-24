package com.wiredi.runtime;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.async.DataAccess;
import com.wiredi.runtime.domain.Scope;
import com.wiredi.runtime.domain.ScopeProvider;
import com.wiredi.runtime.domain.ScopeRegistry;
import com.wiredi.runtime.domain.conditional.ConditionEvaluation;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.IdentifiableProviderSource;
import com.wiredi.runtime.domain.provider.condition.LoadCondition;
import com.wiredi.runtime.lang.Counter;
import com.wiredi.runtime.properties.Key;
import com.wiredi.runtime.time.Timed;
import com.wiredi.runtime.values.Value;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.wiredi.runtime.lang.Ordered.ordered;

public class WireBootstrap {

    private static final Logging logger = Logging.getInstance(WireBootstrap.class);
    private static final String ROUND_LOGGING_PREFIX = "Applied %s conditional providers in %s rounds.";
    private static final String ROUND_WARNING_SUFFIX = " Consider to optimize the condition orders to reduce the rounds required for conditional checks.";
    @NotNull
    private final ScopeRegistry scopeRegistry;
    @NotNull
    private final DataAccess dataAccess = new DataAccess();
    @NotNull
    private final List<@NotNull IdentifiableProviderSource> sources = new ArrayList<>();
    @NotNull
    private final StartupDiagnostics startupDiagnostics;
    private volatile boolean loaded = false;
    private final Value<Integer> conditionalRoundThreshold;
    @NotNull
    private final WireRepository wireRepository;

    public WireBootstrap(@NotNull WireRepository wireRepository) {
        this.scopeRegistry = wireRepository.scopeRegistry();
        this.startupDiagnostics = wireRepository.startupDiagnostics();
        this.conditionalRoundThreshold = Value.lazy(() -> wireRepository.environment().getProperty(PropertyKeys.CONDITIONAL_ROUND_THRESHOLD.getKey(), 10));
        this.wireRepository = wireRepository;
    }

    public void addSource(IdentifiableProviderSource source) {
        this.sources.add(source);
    }

    public void removeSource(IdentifiableProviderSource source) {
        this.sources.remove(source);
    }

    public void addSources(List<? extends IdentifiableProviderSource> sources) {
        this.sources.addAll(sources);
    }

    public void removeSources(List<? extends IdentifiableProviderSource> sources) {
        this.sources.removeAll(sources);
    }

    /**
     * Loads all available {@link IdentifiableProvider}.
     * <p>
     * This method uses the {@link ServiceFiles} to load providers.
     *
     * @return the time it took to load the BeanContainer
     */
    @NotNull
    public Timed load() {
        // pre-check to avoid unnecessary synchronization
        if (loaded) {
            return Timed.ZERO;
        }
        return dataAccess.writeValue(() -> {
            // Check again, to combat race conditions,
            // where both threads pass the pre-checks
            // and then, one after another enter this
            // synchronized statement and then override
            // the same instances.
            // We want to ensure under any
            // circumstances that we only load once.
            if (loaded) {
                return Timed.ZERO;
            }

            ProviderCatalog providerCatalog = new ProviderCatalog();
            logger.debug("Registering all known identifiable providers");
            Timed timed = startupDiagnostics.measure("WireBootstrap.load", () -> {
                Stream<IdentifiableProvider<?>> providers = startupDiagnostics.measure("WireBootstrap.loadProviders", this::loadProviders)
                        .then(timedValue -> logger.debug(() -> "Loaded IdentifiableProviders in " + timedValue.time()))
                        .value();;
                startupDiagnostics.measure("WireBootstrap.constructProviderCatalog", () -> fillProviderCatalog(providerCatalog, providers))
                        .then(t -> logger.debug(() -> "Constructed provider catalog in " + t));
                startupDiagnostics.measure("WireBootstrap.applyConditionals", () -> applyConditionals(providerCatalog))
                        .then(t -> logger.debug("Applied conditionals in " + t));
            });
            logger.debug(() -> "Registered " + providerCatalog.countRegisteredProviders() + "identifiable providers in " + timed);
            loaded = true;
            return timed;
        });
    }

    private void fillProviderCatalog(ProviderCatalog providerCatalog, Stream<IdentifiableProvider<?>> providerStream) {
        providerStream.forEach(provider -> {
            LoadCondition condition = provider.condition();
            if (condition != null) {
                providerCatalog.addConditionalProvider(provider);
            } else {
                registerProviderAtScope(provider, providerCatalog);
            }
        });
    }

    private Scope resolveScope(IdentifiableProvider<?> identifiableProvider) {
        final ScopeProvider scopeProvider = identifiableProvider.scope();
        Scope scope = null;
        if (scopeProvider != null) {
            scope = scopeProvider.getScope(scopeRegistry);
        }

        if (scope == null) {
            scope = scopeRegistry.getDefaultScope();
        }
        return scope;
    }

    private <T> void registerProviderAtScope(
            @NotNull final IdentifiableProvider<T> identifiableProvider,
            @NotNull final ProviderCatalog providerCatalog
    ) {
        Scope scope = resolveScope(identifiableProvider);
        logger.trace(() -> "Registering instance of type " + identifiableProvider.getClass() + " with wired types " + identifiableProvider.additionalWireTypes() + " and qualifiers " + identifiableProvider.qualifiers());
        try {
            scope.register(identifiableProvider);
            providerCatalog.addSuccessfullyRegisteredProvider(identifiableProvider);
        } catch (@NotNull Throwable throwable) {
            providerCatalog.noteError(identifiableProvider, throwable);
        }
    }

    private void applyConditionals(@NotNull ProviderCatalog providerCatalog) {
        List<IdentifiableProvider<?>> conditionalProviders = providerCatalog.conditionalProviders();
        if (conditionalProviders.isEmpty()) {
            return;
        }
        ConditionEvaluation conditionEvaluation = new ConditionEvaluation(wireRepository);

        logger.debug(() -> "Detected " + conditionalProviders.size() + " conditional providers.");
        Counter additionalRounds = new Counter();
        int result = applyConditionals(conditionalProviders, additionalRounds, conditionEvaluation, providerCatalog);
        if (additionalRounds.get() > conditionalRoundThreshold.get()) {
            logger.warn(() -> ROUND_LOGGING_PREFIX.formatted(result, additionalRounds.get()) + ROUND_WARNING_SUFFIX);
        } else {
            logger.debug(() -> ROUND_LOGGING_PREFIX.formatted(result, additionalRounds.get()));
        }

        if (wireRepository.environment().getProperty(Key.just("debug"), false)) {
            System.out.println();
            System.out.println("Condition Evaluation:");
            System.out.println("=====================");
            System.out.println("Total: " + conditionalProviders.size());
            System.out.println("Applied: " + result);
            System.out.println("Additional Rounds: " + additionalRounds.get());
            System.out.println("Round Threshold: " + conditionalRoundThreshold.get());
            System.out.println();
            conditionEvaluation.forEach(context -> {
                System.out.println("# " + context.provider());
                if (!context.positiveMatches().isEmpty()) {
                    System.out.println(" ## Positive Matches ##");
                    context.positiveMatches().forEach(match -> {
                        System.out.println(" - " + match);
                    });
                }

                if (!context.negativeMatches().isEmpty()) {
                    System.out.println(" ## Negative Matches ##");
                    context.negativeMatches().forEach(match -> {
                        System.out.println(" - " + match);
                    });
                }

                System.out.println();
            });
            System.out.println("=====================");
        }
    }

    private int applyConditionals(
            @NotNull List<IdentifiableProvider<?>> identifiableProviders,
            @NotNull Counter round,
            @NotNull ConditionEvaluation conditionEvaluation,
            @NotNull ProviderCatalog providerCatalog
    ) {
        int applied = 0;
        round.increment();
        List<IdentifiableProvider<?>> notMatched = new ArrayList<>();
        logger.debug(() -> "Applying conditional providers; Round " + round.get());

        for (IdentifiableProvider<?> provider : ordered(identifiableProviders)) {
            LoadCondition condition = Objects.requireNonNull(provider.condition());
            ConditionEvaluation.Context context = conditionEvaluation.access(provider);
            context.reset();
            condition.test(context);

            if (context.isMatched()) {
                try {
                    resolveScope(provider).register(provider);
                    providerCatalog.addSuccessfullyRegisteredProvider(provider);
                    applied++;
                } catch (Throwable throwable) {
                    providerCatalog.noteError(provider, throwable);
                }
            } else {
                notMatched.add(provider);
            }
        }

        if (applied > 0 && !notMatched.isEmpty()) {
            int finalApplied = applied;
            logger.debug(() -> "Applied " + finalApplied + " conditions in round " + round.get() + ". Attempting another round for the conditionals on " + notMatched.size() + " left over conditionals.");
            applied += applyConditionals(notMatched, round, conditionEvaluation, providerCatalog);
        }

        return applied;
    }


    private Stream<IdentifiableProvider<?>> loadProviders() {
        return startupDiagnostics.measure("WireBootstrap.loadProviders", () -> sources.stream().flatMap(source -> source.load().stream()))
                .then(timedValue -> logger.debug(() -> "Loaded IdentifiableProviders in " + timedValue.time()))
                .value();
    }

    public boolean isLoaded() {
        return loaded;
    }
}
