package com.wiredi.runtime.beans;

import com.wiredi.domain.WireConflictResolver;
import com.wiredi.domain.provider.IdentifiableProvider;
import com.wiredi.domain.provider.TypeIdentifier;
import com.wiredi.qualifier.QualifierType;
import com.wiredi.runtime.exceptions.MultiplePrimaryProvidersRegisteredException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

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
        this.rootType = rootType;
        this.unqualifiedProviders = unqualifiedProviders;
    }

    @Override
    public List<IdentifiableProvider<T>> getAll() {
        List<IdentifiableProvider<T>> result = getAllUnqualified();
        result.addAll(qualifiedProviders.values());
        if (primary != null && !result.contains(primary)) {
            result.add(primary);
        }
        return result;
    }

    @Override
    public List<IdentifiableProvider<T>> getAllUnqualified() {
        List<IdentifiableProvider<T>> result = new ArrayList<>(unqualifiedProviders);
        result.addAll(typedUnqualifiedProviders.values().stream().map(TypedProviderState::determine).toList());
        return result;
    }

    @Override
    public List<IdentifiableProvider<T>> getAllQualified() {
        return List.copyOf(new HashSet<>(qualifiedProviders.values()));
    }

    @Override
    public Optional<IdentifiableProvider<T>> get(QualifierType qualifierType) {
        return Optional.ofNullable(qualifiedProviders.get(qualifierType));
    }

    @Override
    public boolean isEmpty() {
        return primary == null || typedUnqualifiedProviders.isEmpty() || qualifiedProviders.isEmpty();
    }

    @Override
    public Optional<IdentifiableProvider<T>> get(TypeIdentifier<T> concreteType, Supplier<WireConflictResolver> conflictResolver) {
        if (primary != null) {
            return Optional.of(primary);
        }
        if (concreteType.willErase()) {
            TypedProviderState<T> state = typedUnqualifiedProviders.get(concreteType);
            if (state != null) {
                return Optional.of(state.determine());
            }
        } if (unqualifiedProviders.size() == 1) {
            return Optional.ofNullable(unqualifiedProviders.iterator().next());
        }

        return fallback(concreteType, conflictResolver);
    }

    private Optional<IdentifiableProvider<T>> fallback(TypeIdentifier<T> concreteType, Supplier<WireConflictResolver> conflictResolver) {
        List<IdentifiableProvider<T>> all = getAll();
        if (all.isEmpty()) {
            return Optional.empty();
        }
        if (all.size() > 1) {
            IdentifiableProvider<T> provider = conflictResolver.get().find(all, concreteType);
            return Optional.of(provider);
        }
        return Optional.of(all.get(0));
    }

    public static class TypedProviderState<T> {
        @NotNull
        private final IdentifiableProvider<T> provider;

        @NotNull
        private final TypeIdentifier<T> concreteType;

        @Nullable
        private IdentifiableProvider<T> primary = null;

        public TypedProviderState(@NotNull IdentifiableProvider<T> provider, @NotNull TypeIdentifier<T> concreteType) {
            this.provider = provider;
            this.concreteType = concreteType;
        }

        @NotNull
        public IdentifiableProvider<T> determine() {
            return Objects.requireNonNullElse(primary, provider);
        }

        public void trySetAsPrimary(IdentifiableProvider<T> identifiableProvider) {
            if (!identifiableProvider.primary()) {
                return;
            }

            if (primary != null) {
                throw new MultiplePrimaryProvidersRegisteredException(concreteType, primary, identifiableProvider);
            }

            primary = identifiableProvider;
        }
    }
}
