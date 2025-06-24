package com.wiredi.runtime.beans;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.ServiceFiles;
import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.async.DataAccess;
import com.wiredi.runtime.beans.value.BeanValue;
import com.wiredi.runtime.domain.conditional.ConditionEvaluation;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.IdentifiableProviderSource;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.domain.provider.condition.LoadCondition;
import com.wiredi.runtime.lang.Counter;
import com.wiredi.runtime.properties.Key;
import com.wiredi.runtime.qualifier.QualifierType;
import com.wiredi.runtime.time.Timed;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.wiredi.runtime.lang.Ordered.ordered;
import static com.wiredi.runtime.lang.Preconditions.is;

/**
 * The BeanContainer holds instances of {@link Bean} containers, which hold Component instances of same types.
 * <p>
 * A BeanContainer is empty by default, but by calling {@link #load(WireRepository)} it is filled with
 * {@link IdentifiableProvider} instances that can be loaded using the {@link ServiceFiles}.
 * It requires a {@link WireRepository} to correctly resolve conditions.
 * <p>
 * All methods of a BeanContainer are supposed to never return null.
 * Even if a Bean is absent, instead of returning null an {@link EmptyModifiableBean} is returned.
 */
public class BeanContainer {

    private static final String ROUND_LOGGING_PREFIX = "Applied %s conditional providers in %s rounds.";
    private static final String ROUND_WARNING_SUFFIX = " Consider to optimize the condition orders to reduce the rounds required for conditional checks.";
    private final Logging logger = Logging.getInstance(BeanContainer.class);
    @NotNull
    private final DataAccess dataAccess = new DataAccess();
    @NotNull
    private final Map<TypeIdentifier<?>, ModifiableBean<?>> mapping = new HashMap<>();
    @NotNull
    private final BeanContainerProperties properties;
    @NotNull
    private final List<@NotNull IdentifiableProviderSource> sources = new ArrayList<>();
    private volatile boolean loaded = false;

    public BeanContainer(
            @NotNull BeanContainerProperties properties
    ) {
        this(properties, List.of(IdentifiableProviderSource.serviceLoader()));
    }

    public BeanContainer(
            @NotNull BeanContainerProperties properties,
            @NotNull List<? extends IdentifiableProviderSource> sources
    ) {
        this.properties = properties;
        this.sources.addAll(sources);
    }

    @NotNull
    public List<@NotNull IdentifiableProviderSource> sources() {
        return Collections.unmodifiableList(sources);
    }

    public void setSources(@NotNull List<? extends IdentifiableProviderSource> sources) {
        this.sources.clear();
        this.sources.addAll(sources);
    }

    public void addSource(@NotNull IdentifiableProviderSource source) {
        this.sources.add(source);
    }

    public void addSources(@NotNull List<? extends IdentifiableProviderSource> sources) {
        this.sources.addAll(sources);
    }

    @NotNull
    public BeanContainerProperties properties() {
        return properties;
    }

    /**
     * Clears the internally loaded and maintained beans.
     * <p>
     * It's not recommended to call this method during normal operations.
     * Instead, the state of the {@link BeanContainer} should be managed by the {@link WireRepository#clear()} method.
     */
    public void clear() {
        dataAccess.write(() -> {
            logger.debug("Clearing cached mappings");
            mapping.clear();
            loaded = false;
        });
    }

    /**
     * Loads all available {@link IdentifiableProvider}.
     * <p>
     * This method uses the {@link ServiceFiles} to load providers.
     *
     * @param wireRepository the repository maintaining this BeanContainer.
     *                       It is required to resolve conditions
     * @return the time it took to load the BeanContainer
     */
    @NotNull
    public Timed load(@NotNull WireRepository wireRepository) {
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

            Counter count = new Counter();
            logger.debug("Registering all known identifiable providers");
            Timed timed = Timed.of(() -> {
                List<IdentifiableProvider> conditionalProviders = new ArrayList<>();
                sources.stream().flatMap(source -> source.load().stream())
                        .forEach(provider -> {
                            LoadCondition condition = provider.condition();
                            if (condition != null) {
                                conditionalProviders.add(provider);
                            } else {
                                count.increment();
                                unsafeRegister(provider);
                            }
                        });

                count.increment(applyConditionals(conditionalProviders, wireRepository));
                loaded = true;
            });
            logger.debug(() -> "Registered " + count.get() + "identifiable providers in " + timed);

            return timed;
        });
    }

    private int applyConditionals(
            @NotNull List<IdentifiableProvider> conditionalProviders,
            @NotNull WireRepository wireRepository
    ) {
        if (conditionalProviders.isEmpty()) {
            return 0;
        }
        ConditionEvaluation conditionEvaluation = new ConditionEvaluation(wireRepository);

        logger.debug(() -> "Detected " + conditionalProviders.size() + " conditional providers.");
        Counter additionalRounds = new Counter();
        int result = applyConditionals(conditionalProviders, wireRepository, additionalRounds, conditionEvaluation);
        int warnThreshHold = properties.conditionalRoundThreshold();
        if (additionalRounds.get() > warnThreshHold) {
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
            System.out.println("Round Threshold: " + warnThreshHold);
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

        return result;
    }

    private int applyConditionals(
            @NotNull List<IdentifiableProvider> identifiableProviders,
            @NotNull WireRepository wireRepository,
            @NotNull Counter round,
            @NotNull ConditionEvaluation conditionEvaluation
    ) {
        int applied = 0;
        round.increment();
        List<IdentifiableProvider> notMatched = new ArrayList<>();
        logger.debug(() -> "Applying conditional providers; Round " + round.get());

        for (IdentifiableProvider provider : ordered(identifiableProviders)) {
            LoadCondition condition = Objects.requireNonNull(provider.condition());
            ConditionEvaluation.Context context = conditionEvaluation.access(provider);
            context.reset();
            condition.test(context);

            if (context.isMatched()) {
                applied += 1;
                unsafeRegister(provider);
            } else {
                notMatched.add(provider);
            }
        }

        if (applied > 0 && !notMatched.isEmpty()) {
            int finalApplied = applied;
            logger.debug(() -> "Applied " + finalApplied + " conditions in round " + round.get() + ". Attempting another round for the conditionals on " + notMatched.size() + " left over conditionals.");
            applied += applyConditionals(notMatched, wireRepository, round, conditionEvaluation);
        }

        return applied;
    }

    public final <T> void register(
            @NotNull final IdentifiableProvider<T> t
    ) {
        dataAccess.write(() -> unsafeRegister(t));
    }

    public final void registerAll(
            @NotNull final List<@NotNull IdentifiableProvider> list
    ) {
        dataAccess.write(() -> list.forEach(this::unsafeRegister));
    }

    private <T> void unsafeRegister(
            @NotNull final IdentifiableProvider<T> t
    ) {
        logger.trace(() -> "Registering instance of type " + t.getClass() + " with wired types " + t.additionalWireTypes() + " and qualifiers " + t.qualifiers());
        for (final TypeIdentifier<?> wiredType : t.additionalWireTypes()) {
            logger.trace(() -> "Registering " + t + " for " + wiredType);
            unsafeGetOrCreate(wiredType).register((TypeIdentifier) wiredType, (IdentifiableProvider) t);
        }
        unsafeGetOrCreate(t.type()).register((TypeIdentifier) t.type(), (IdentifiableProvider) t);
    }

    public final boolean isLoaded() {
        return loaded;
    }

    @NotNull
    public <T> Bean<T> access(
            @NotNull TypeIdentifier<T> typeIdentifier
    ) {
        is(typeIdentifier.referenceConcreteType(), () -> "Cannot call access on a reference type (Bean, IdentifiableProvider): " + typeIdentifier);
        return dataAccess.readValue(() -> unsafeGet(typeIdentifier))
                .orElse(ModifiableBean.empty());
    }

    @NotNull
    public <T> Bean<T> accessOrCreate(
            @NotNull TypeIdentifier<T> typeIdentifier
    ) {
        is(typeIdentifier.referenceConcreteType(), () -> "Cannot call accessOrCreate on a reference type (Bean, IdentifiableProvider): " + typeIdentifier);
        return dataAccess.readValue(() -> unsafeGet(typeIdentifier))
                .orElseGet(() -> dataAccess.writeValue(() -> (ModifiableBean<T>) unsafeGetOrCreate(typeIdentifier)));
    }

    @NotNull
    public <T> BeanValue<T> get(
            @NotNull final TypeIdentifier<T> concreteType
    ) {
        is(concreteType.referenceConcreteType(), () -> "Cannot call get on a reference type (Bean, IdentifiableProvider): " + concreteType);
        return dataAccess.readValue(() -> unsafeGet(concreteType))
                .get(concreteType, properties.wireConflictResolverSupplier());
    }

    @NotNull
    public <T> BeanValue<T> get(
            @NotNull final TypeIdentifier<T> typeIdentifier,
            @NotNull QualifierType qualifierType
    ) {
        is(typeIdentifier.referenceConcreteType(), () -> "Cannot call get on a reference type (Bean, IdentifiableProvider): " + typeIdentifier);
        return dataAccess.readValue(() -> unsafeGet(typeIdentifier))
                .get(qualifierType);
    }

    @NotNull
    public <T> List<IdentifiableProvider<T>> getAll(
            @NotNull final TypeIdentifier<T> typeIdentifier
    ) {
        is(typeIdentifier.referenceConcreteType(), () -> "Cannot call getAll on a reference type (Bean, IdentifiableProvider): " + typeIdentifier);
        ModifiableBean<T> bean = dataAccess.readValue(() -> unsafeGet(typeIdentifier));
        if (typeIdentifier.equals(bean.rootType())) {
            return bean.getAll();
        } else {
            return bean.getAll(typeIdentifier);
        }
    }

    @NotNull
    private <T> ModifiableBean<T> unsafeGet(
            @NotNull final TypeIdentifier<T> type
    ) {
        return (ModifiableBean<T>) mapping.getOrDefault(type.erasure(), ModifiableBean.empty());
    }

    @NotNull
    private ModifiableBean<?> unsafeGetOrCreate(
            @NotNull final TypeIdentifier<?> type
    ) {
        return mapping.computeIfAbsent(type.erasure(), t -> new ModifiableBean<>(type));
    }

    public int size() {
        return mapping.size();
    }

    @Override
    @NotNull
    public final String toString() {
        return getClass().getSimpleName() + "{" +
                "loaded=" + loaded +
                '}';
    }
}
