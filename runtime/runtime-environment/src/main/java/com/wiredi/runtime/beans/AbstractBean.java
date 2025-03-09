package com.wiredi.runtime.beans;

import com.wiredi.runtime.beans.value.BeanValue;
import com.wiredi.runtime.domain.WireConflictResolver;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.exceptions.MultiplePrimaryProvidersRegisteredException;
import com.wiredi.runtime.qualifier.QualifierType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractBean<T> implements Bean<T> {

    protected final Map<QualifierType, IdentifiableProvider<T>> qualifiedProviders;
    protected final Map<TypeIdentifier<T>, TypedProviderState<T>> typedUnqualifiedProviders;
    protected final Set<IdentifiableProvider<T>> unqualifiedProviders;
    protected final TypeIdentifier<T> rootType;

    @Nullable
    protected IdentifiableProvider<T> primary;

    protected AbstractBean(TypeIdentifier<T> rootType) {
        this(new HashMap<>(), new HashMap<>(), new HashSet<>(), rootType);
    }

    protected AbstractBean(
            Map<QualifierType, IdentifiableProvider<T>> qualifiedProviders,
            Map<TypeIdentifier<T>, TypedProviderState<T>> typedUnqualifiedProviders,
            Set<IdentifiableProvider<T>> unqualifiedProviders,
            TypeIdentifier<T> rootType
    ) {
        this.qualifiedProviders = qualifiedProviders;
        this.typedUnqualifiedProviders = typedUnqualifiedProviders;
        this.rootType = rootType.erasure();
        this.unqualifiedProviders = unqualifiedProviders;
    }

    @Override
    public @NotNull TypeIdentifier<T> rootType() {
        return this.rootType;
    }

    @Override
    public @NotNull List<IdentifiableProvider<T>> getAll() {
        Set<IdentifiableProvider<T>> result = join(getAllUnqualified(), getAllQualified());
        if (primary != null) {
            result.add(primary);
        }
        return new ArrayList<>(result);
    }

    @Override
    public @NotNull List<IdentifiableProvider<T>> getAll(TypeIdentifier<T> concreteType) {
        return new ArrayList<>(join(getAllQualified(concreteType), getAllUnqualified(concreteType)));
    }

    @Override
    public @NotNull List<IdentifiableProvider<T>> getAllUnqualified() {
        List<IdentifiableProvider<T>> result = new ArrayList<>(unqualifiedProviders);
        result.addAll(typedUnqualifiedProviders.values().stream().flatMap(it -> it.all().stream()).toList());
        return result;
    }

    @Override
    public @NotNull List<IdentifiableProvider<T>> getAllUnqualified(TypeIdentifier<T> concreteType) {
        List<IdentifiableProvider<T>> result = new ArrayList<>(unqualifiedProviders.stream()
                .filter(it -> concreteType.isInstanceOf(it.type()))
                .toList());
        result.addAll(
                typedUnqualifiedProviders.entrySet()
                        .stream()
                        .filter(entry -> entry.getKey().isInstanceOf(concreteType))
                        .flatMap(it -> it.getValue().all().stream())
                        .toList()
        );
        return result;
    }

    @Override
    public @NotNull List<IdentifiableProvider<T>> getAllQualified() {
        return List.copyOf(new HashSet<>(qualifiedProviders.values()));
    }

    @Override
    public @NotNull List<IdentifiableProvider<T>> getAllQualified(TypeIdentifier<T> typeIdentifier) {
        return qualifiedProviders.values()
                .stream()
                .filter(it -> typeIdentifier.isInstanceOf(it.type()))
                .toList();
    }

    @Override
    public @NotNull BeanValue<T> get(QualifierType qualifierType) {
        return BeanValue.of(qualifiedProviders.get(qualifierType));
    }

    @Override
    public boolean isEmpty() {
        return !hasPrimary()
                && !hasUnqualifiedProviders()
                && !hasQualifiedProviders();
    }

    public boolean hasQualifiedProviders() {
        return !qualifiedProviders.isEmpty();
    }

    public boolean hasUnqualifiedProviders() {
        return !unqualifiedProviders.isEmpty();
    }

    public boolean hasPrimary() {
        return primary != null;
    }

    @Override
    public int size() {
        return (primary != null ? 1 : 0) + unqualifiedProviders.size() + qualifiedProviders.size();
    }

    @Override
    public @NotNull BeanValue<T> get(TypeIdentifier<T> concreteType, Supplier<WireConflictResolver> conflictResolver) {
        if (primary != null) {
            return BeanValue.of(primary);
        }
        if (concreteType.willErase()) {
            TypedProviderState<T> state = typedUnqualifiedProviders.get(concreteType);
            if (state != null) {
                return BeanValue.of(state.determine(conflictResolver));
            }
        }
        if (unqualifiedProviders.size() == 1) {
            return BeanValue.of(unqualifiedProviders.iterator().next());
        }

        return fallback(concreteType, conflictResolver);
    }

    @Override
    public @NotNull BeanValue<T> get(Supplier<WireConflictResolver> conflictResolver) {
        if (primary != null) {
            return BeanValue.of(primary);
        }

        if (unqualifiedProviders.size() == 1) {
            Iterator<IdentifiableProvider<T>> iterator = unqualifiedProviders.iterator();
            IdentifiableProvider<T> result = iterator.next();
            if (iterator.hasNext()) {
                throw new ConcurrentModificationException("Bean was updated while fetching Identifiable Providers of " + rootType);
            }
            return BeanValue.of(result);
        }

        return fallback(rootType, conflictResolver);
    }

    private BeanValue<T> fallback(TypeIdentifier<T> concreteType, Supplier<WireConflictResolver> conflictResolver) {
        List<IdentifiableProvider<T>> all = getAll();
        if (all.isEmpty()) {
            return BeanValue.empty();
        }
        if (all.size() > 1) {
            IdentifiableProvider<T> provider = conflictResolver.get().find(all, concreteType);
            return BeanValue.of(provider);
        }
        return BeanValue.of(all.getFirst());
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

        public TypedProviderState<T> add(IdentifiableProvider<T> identifiableProvider) {
            this.providers.add(identifiableProvider);
            return this;
        }

        public TypedProviderState<T> trySetAsPrimary(IdentifiableProvider<T> identifiableProvider) {
            if (!identifiableProvider.primary()) {
                return this;
            }

            if (primary != null) {
                throw new MultiplePrimaryProvidersRegisteredException(concreteType, primary, identifiableProvider);
            }

            primary = identifiableProvider;

            return this;
        }
    }

    private <S> Set<S> join(Collection<S> a, Collection<S> b) {
        HashSet<S> result = new HashSet<>();
        result.addAll(a);
        result.addAll(b);
        return result;
    }
}
