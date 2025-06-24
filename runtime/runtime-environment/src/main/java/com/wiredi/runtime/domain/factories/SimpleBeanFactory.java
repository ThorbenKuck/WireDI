package com.wiredi.runtime.domain.factories;

import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.beans.AbstractBean;
import com.wiredi.runtime.domain.BeanFactory;
import com.wiredi.runtime.domain.StandardWireConflictResolver;
import com.wiredi.runtime.domain.WireConflictResolver;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.exceptions.DiLoadingException;
import com.wiredi.runtime.exceptions.MultiplePrimaryProvidersRegisteredException;
import com.wiredi.runtime.exceptions.MultipleSameQualifierProviderRegisteredExceptions;
import com.wiredi.runtime.lang.OrderedComparator;
import com.wiredi.runtime.qualifier.QualifierType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class SimpleBeanFactory<T> implements BeanFactory<T> {

    @NotNull
    private final Map<QualifierType, IdentifiableProvider<T>> qualifiedProviders;
    @NotNull
    private final Map<TypeIdentifier<T>, TypedProviderState<T>> typedUnqualifiedProviders;
    @NotNull
    private final TypeIdentifier<T> rootType;
    @Nullable
    protected IdentifiableProvider<T> primary;
    @NotNull
    private Supplier<@NotNull WireConflictResolver> conflictResolver = () -> StandardWireConflictResolver.DEFAULT;

    public SimpleBeanFactory(@NotNull TypeIdentifier<T> rootType) {
        this.qualifiedProviders = new HashMap<>();
        this.typedUnqualifiedProviders = new HashMap<>();
        this.rootType = rootType;
    }

    protected SimpleBeanFactory(@NotNull SimpleBeanFactory<T> simpleBeanFactory) {
        this.primary = simpleBeanFactory.primary;
        this.qualifiedProviders = new HashMap<>(simpleBeanFactory.qualifiedProviders);
        this.typedUnqualifiedProviders = new HashMap<>(simpleBeanFactory.typedUnqualifiedProviders);
        this.rootType = simpleBeanFactory.rootType;
    }

    public SimpleBeanFactory<T> withConflictResolver(@NotNull Supplier<@NotNull WireConflictResolver> conflictResolver) {
        this.conflictResolver = conflictResolver;
        return this;
    }

    @Override
    public @NotNull TypeIdentifier<T> rootType() {
        return rootType;
    }

    @Override
    public @NotNull Collection<Bean<T>> getAll(@NotNull WireRepository wireRepository) {
        return getAll(wireRepository, rootType);
    }

    @Override
    public @NotNull Collection<Bean<T>> getAll(@NotNull WireRepository wireRepository, @NotNull TypeIdentifier<T> type) {
        return join(getAllQualified(type), getAllUnqualified(type))
                .stream()
                .sorted(OrderedComparator.INSTANCE)
                .map(it -> new Bean<>(it.get(wireRepository, type), it))
                .toList();
    }

    @Override
    public @Nullable Bean<T> get(@NotNull WireRepository wireRepository, @NotNull TypeIdentifier<T> concreteType) {
        if (primary != null) {
            return new Bean<>(primary.get(wireRepository, concreteType), primary);
        }

        if (concreteType.willErase()) {
            TypedProviderState<T> state = typedUnqualifiedProviders.get(concreteType);
            if (state != null) {
                IdentifiableProvider<T> current = state.determine(this.conflictResolver);
                if (current != null) {
                    return new Bean<>(current.get(wireRepository, concreteType), current);
                }
            }
        }

        if (this.typedUnqualifiedProviders.size() == 1) {
            TypedProviderState<T> next = this.typedUnqualifiedProviders.values().iterator().next();
            List<IdentifiableProvider<T>> all = next.all();
            if (all.size() == 1) {
                IdentifiableProvider<T> first = all.getFirst();
                return new Bean<>(first.get(wireRepository, concreteType), first);
            }
        }

        return fallback(wireRepository, concreteType, conflictResolver);
    }

    protected @Nullable Bean<T> fallback(
            @NotNull WireRepository wireRepository,
            @NotNull TypeIdentifier<T> concreteType,
            @NotNull Supplier<WireConflictResolver> conflictResolver
    ) {
        Set<IdentifiableProvider<T>> all = join(getAllUnqualified(), getAllQualified());
        if (all.isEmpty()) {
            return null;
        }

        IdentifiableProvider<T> provider;
        if (all.size() == 1) {
            provider = all.iterator().next();
        } else {
            provider = conflictResolver.get().find(all, concreteType);
        }

        T instance = provider.get(wireRepository, concreteType);

        if (instance == null) {
            return null;
        }

        return new Bean<>(instance, provider);
    }


    @Override
    public @Nullable Bean<T> get(@NotNull WireRepository wireRepository, @NotNull TypeIdentifier<T> type, @NotNull QualifierType qualifierType) {
        return Optional.ofNullable(qualifiedProviders.get(qualifierType))
                .map(it -> new Bean<>(it.get(wireRepository, type), it))
                .orElse(null);
    }

    @Override
    public void register(@NotNull IdentifiableProvider<T> identifiableProvider) {
        TypeIdentifier type = identifiableProvider.type();
        if (identifiableProvider.qualifiers().isEmpty()) {
            if (identifiableProvider.primary()) {
                registerPrimaryProvider(type, identifiableProvider);
            } else {
                addUnqualifiedProvider(type, identifiableProvider);
            }
        } else {
            addQualifiedProvider(identifiableProvider, identifiableProvider.qualifiers());

            if (identifiableProvider.primary()) {
                registerPrimaryProvider(type, identifiableProvider);
            }
        }
    }

    /**
     * Sets the provided {@link IdentifiableProvider} as the primary identifiable provider.
     * <p>
     * This method will bypass the check, whether the provided {@link IdentifiableProvider} is primary or not and hard
     * sets the provider as primary.
     * It still validates that no other primary identifiable provider is set.
     *
     * @param identifiableProvider the provider to set as the primary
     * @throws DiLoadingException if the Bean already has a primary {@link IdentifiableProvider}
     */
    public void registerPrimaryProvider(TypeIdentifier<T> concreteType, IdentifiableProvider<T> identifiableProvider) {
        if (concreteType.willErase()) {
            typedUnqualifiedProviders.computeIfAbsent(concreteType, t -> new TypedProviderState<>(concreteType))
                    .add(identifiableProvider);
        } else {
            if (primary != null) {
                throw new MultiplePrimaryProvidersRegisteredException(concreteType, primary, identifiableProvider);
            }

            primary = identifiableProvider;
        }
    }

    /**
     * Adds the provided {@link IdentifiableProvider} as an unqualified provider.
     * <p>
     * This method will bypass any checks and just add this provider as an unqualified one.
     *
     * @param identifiableProvider the provider to set as the primary
     */
    public void addUnqualifiedProvider(TypeIdentifier<T> concreteType, IdentifiableProvider<T> identifiableProvider) {
        typedUnqualifiedProviders.computeIfAbsent(concreteType, t -> new TypedProviderState<>(concreteType)).add(identifiableProvider);
    }

    /**
     * Sets the provided {@link IdentifiableProvider} as a qualified bean for all {@link QualifierType qualifiers}.
     * <p>
     * This method will bypass the check, whether the provided {@link IdentifiableProvider} is normally qualified and
     * matching the qualifiers under {@link IdentifiableProvider#qualifiers()}
     * It still validates that no other primary identifiable provider is registered for the qualifier.
     *
     * @param newProvider the provider to set as the primary
     * @param qualifiers  the qualifiers which the provider should be registered to
     * @throws DiLoadingException if the Bean already has a primary {@link IdentifiableProvider}
     */
    public void addQualifiedProvider(IdentifiableProvider<T> newProvider, List<QualifierType> qualifiers) {
        for (QualifierType qualifier : qualifiers) {
            IdentifiableProvider<T> existingProvider = qualifiedProviders.get(qualifier);
            if (existingProvider != null) {
                throw new MultipleSameQualifierProviderRegisteredExceptions(qualifier, newProvider, existingProvider);
            }

            qualifiedProviders.put(qualifier, newProvider);
        }
    }

    protected @NotNull List<IdentifiableProvider<T>> getAllQualified(TypeIdentifier<T> typeIdentifier) {
        return qualifiedProviders.values()
                .stream()
                .filter(it -> typeIdentifier.isInstanceOf(it.type()))
                .toList();
    }

    protected @NotNull List<IdentifiableProvider<T>> getAllQualified() {
        return List.copyOf(new HashSet<>(qualifiedProviders.values()));
    }

    protected @NotNull List<IdentifiableProvider<T>> getAllUnqualified(TypeIdentifier<T> concreteType) {
        return typedUnqualifiedProviders.entrySet()
                .stream()
                .filter(entry -> entry.getKey().isInstanceOf(concreteType))
                .flatMap(it -> it.getValue().all().stream())
                .toList();
    }

    protected @NotNull List<IdentifiableProvider<T>> getAllUnqualified() {
        return typedUnqualifiedProviders.values()
                .stream()
                .flatMap(it -> it.all().stream())
                .toList();
    }

    private <S> Set<S> join(Collection<S> a, Collection<S> b) {
        HashSet<S> result = new HashSet<>();
        result.addAll(a);
        result.addAll(b);
        return result;
    }

    public static class TypedProviderState<T> {
        @NotNull
        private final List<IdentifiableProvider<T>> providers = new ArrayList<>();

        @NotNull
        private final TypeIdentifier<T> concreteType;

        @Nullable
        private IdentifiableProvider<T> primary = null;

        public TypedProviderState(@NotNull TypeIdentifier<T> concreteType) {
            this.concreteType = concreteType;
        }

        @Nullable
        public IdentifiableProvider<T> determine(Supplier<WireConflictResolver> conflictResolver) {
            if (primary != null) {
                return primary;
            }
            if (providers.isEmpty()) {
                return null;
            }
            if (providers.size() == 1) {
                return providers.getFirst();
            }

            return conflictResolver.get().find(providers, concreteType);
        }

        @NotNull
        public List<IdentifiableProvider<T>> all() {
            return providers;
        }

        @NotNull
        public List<IdentifiableProvider<T>> all(TypeIdentifier<T> typeIdentifier) {
            return providers.stream()
                    .filter(it -> typeIdentifier.isInstanceOf(it.type()))
                    .toList();
        }

        public void add(IdentifiableProvider<T> identifiableProvider) {
            if (identifiableProvider.primary()) {
                if (primary != null) {
                    throw new MultiplePrimaryProvidersRegisteredException(concreteType, primary, identifiableProvider);
                } else {
                    primary = identifiableProvider;
                }
            } else {
                this.providers.add(identifiableProvider);
            }
        }
    }
}
