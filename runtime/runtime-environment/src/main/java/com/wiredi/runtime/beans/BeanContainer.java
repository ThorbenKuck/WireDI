package com.wiredi.runtime.beans;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.ServiceLoader;
import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.async.DataAccess;
import com.wiredi.runtime.beans.value.BeanValue;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.domain.provider.condition.LoadCondition;
import com.wiredi.runtime.lang.Counter;
import com.wiredi.runtime.qualifier.QualifierType;
import com.wiredi.runtime.time.Timed;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wiredi.runtime.domain.Ordered.ordered;
import static com.wiredi.runtime.lang.Preconditions.is;

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
    private final ServiceLoader loader;
    private volatile boolean loaded = false;

    public BeanContainer(
            @NotNull BeanContainerProperties properties,
            @NotNull ServiceLoader loader
    ) {
        this.properties = properties;
        this.loader = loader;
    }

    public BeanContainerProperties properties() {
        return properties;
    }

    public void clear() {
        dataAccess.write(() -> {
            logger.debug("Clearing cached mappings");
            mapping.clear();
            loaded = false;
        });
    }

    public Timed load(WireRepository wireRepository) {
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
            // We want to ensure under any and all
            // circumstances that we only load once.
            if (loaded) {
                return Timed.ZERO;
            }


            Counter count = new Counter();
            logger.debug("Registering all known identifiable providers");
            Timed timed = Timed.of(() -> {
                List<IdentifiableProvider> conditionalProviders = new ArrayList<>();
                loader.identifiableProviders().forEach(provider -> {
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

    private int applyConditionals(List<IdentifiableProvider> conditionalProviders, WireRepository wireRepository) {
        if (conditionalProviders.isEmpty()) {
            return 0;
        }

        logger.debug(() -> "Detected " + conditionalProviders.size() + " conditional providers.");
        Counter additionalRounds = new Counter();
        int result = applyConditionals(conditionalProviders, wireRepository, additionalRounds);
        int warnThreshHold = properties.conditionalRoundThreshold();
        if (additionalRounds.get() >= warnThreshHold) {
            logger.warn(() -> ROUND_LOGGING_PREFIX.formatted(result, additionalRounds.get()) + ROUND_WARNING_SUFFIX);
        } else {
            logger.debug(() -> ROUND_LOGGING_PREFIX.formatted(result, additionalRounds.get()));
        }
        return result;
    }

    private int applyConditionals(
            List<IdentifiableProvider> identifiableProviders,
            WireRepository wireRepository,
            Counter round
    ) {
        int applied = 0;
        round.increment();
        List<IdentifiableProvider> notMatched = new ArrayList<>();
        logger.debug(() -> "Applying conditional providers; Round " + round.get());

        for (IdentifiableProvider provider : ordered(identifiableProviders)) {
            LoadCondition condition = provider.condition();
            if (condition != null && condition.matches(wireRepository)) {
                applied += 1;
                unsafeRegister(provider);
            } else {
                notMatched.add(provider);
            }
        }

        if (applied > 0 && !notMatched.isEmpty()) {
            int finalApplied = applied;
            logger.debug(() -> "Applied " + finalApplied + " conditions in round " + round.get() + ". Attempting another round for the conditionals on " + notMatched.size() + " left over conditionals.");
            applied += applyConditionals(notMatched, wireRepository, round);
        }

        return applied;
    }

    public final <T> void register(@NotNull final IdentifiableProvider<T> t) {
        dataAccess.write(() -> unsafeRegister(t));
    }

    public final void registerAll(@NotNull final List<@NotNull IdentifiableProvider> list) {
        dataAccess.write(() -> list.forEach(this::unsafeRegister));
    }

    private <T> void unsafeRegister(@NotNull final IdentifiableProvider<T> t) {
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

    public <T> Bean<T> access(TypeIdentifier<T> typeIdentifier) {
        is(typeIdentifier.referenceConcreteType(), () -> "Cannot call access on a reference type (Bean, IdentifiableProvider): " + typeIdentifier);
        return dataAccess.readValue(() -> unsafeGet(typeIdentifier))
                .orElse(ModifiableBean.empty());
    }

    public <T> Bean<T> accessOrCreate(TypeIdentifier<T> typeIdentifier) {
        is(typeIdentifier.referenceConcreteType(), () -> "Cannot call accessOrCreate on a reference type (Bean, IdentifiableProvider): " + typeIdentifier);
        return dataAccess.readValue(() -> unsafeGet(typeIdentifier))
                .orElseGet(() -> dataAccess.writeValue(() -> (ModifiableBean<T>) unsafeGetOrCreate(typeIdentifier)));
    }

    public <T> BeanValue<T> get(final TypeIdentifier<T> concreteType) {
        is(concreteType.referenceConcreteType(), () -> "Cannot call get on a reference type (Bean, IdentifiableProvider): " + concreteType);
        return dataAccess.readValue(() -> unsafeGet(concreteType))
                .get(concreteType, properties.wireConflictResolverSupplier());
    }

    public <T> BeanValue<T> get(final TypeIdentifier<T> typeIdentifier, QualifierType qualifierType) {
        is(typeIdentifier.referenceConcreteType(), () -> "Cannot call get on a reference type (Bean, IdentifiableProvider): " + typeIdentifier);
        return dataAccess.readValue(() -> unsafeGet(typeIdentifier))
                .get(qualifierType);
    }

    public <T> List<IdentifiableProvider<T>> getAll(final TypeIdentifier<T> typeIdentifier) {
        is(typeIdentifier.referenceConcreteType(), () -> "Cannot call getAll on a reference type (Bean, IdentifiableProvider): " + typeIdentifier);
        ModifiableBean<T> bean = dataAccess.readValue(() -> unsafeGet(typeIdentifier));
        if (typeIdentifier.equals(bean.rootType())) {
            return bean.getAll();
        } else {
            return bean.getAll(typeIdentifier);
        }
    }

    private <T> ModifiableBean<T> unsafeGet(@NotNull final TypeIdentifier<T> type) {
        return (ModifiableBean<T>) mapping.getOrDefault(type.erasure(), ModifiableBean.empty());
    }

    private ModifiableBean<?> unsafeGetOrCreate(@NotNull final TypeIdentifier<?> type) {
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
