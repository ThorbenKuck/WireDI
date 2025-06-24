package com.wiredi.runtime.domain.scopes;

import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.Scope;
import com.wiredi.runtime.domain.factories.MissingBeanException;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.QualifiedTypeIdentifier;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CompositeScope implements Scope {

    private final List<Scope> scopes = new ArrayList<>();

    public CompositeScope(Collection<Scope> scopes) {
        this.scopes.addAll(scopes);
    }

    @Override
    public <T> @NotNull Optional<T> tryGet(@NotNull QualifiedTypeIdentifier<T> qualifierType) {
        for (Scope scope : scopes) {
            if (scope.canSupply(qualifierType)) {
                Optional<T> optional = scope.tryGet(qualifierType);
                if (optional.isPresent()) {
                    return optional;
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public <T> @NotNull T get(@NotNull QualifiedTypeIdentifier<T> qualifierType) {
        for (Scope scope : scopes) {
            if (scope.canSupply(qualifierType)) {
                return scope.get(qualifierType);
            }
        }

        throw MissingBeanException.unableToCreate(qualifierType.type());
    }

    @Override
    public boolean contains(@NotNull QualifiedTypeIdentifier<?> type) {
        return scopes.stream().anyMatch(it -> it.contains(type));
    }

    @Override
    public <T> @NotNull Collection<T> getAll(@NotNull TypeIdentifier<T> type) {
        for (Scope scope : scopes) {
            Collection<T> all = scope.getAll(type);
            if (!all.isEmpty()) {
                return all;
            }
        }

        return Collections.emptyList();
    }

    @Override
    public void register(@NotNull IdentifiableProvider<?> provider) {
        scopes.forEach(it -> it.register(provider));
    }

    @Override
    public boolean canSupply(@NotNull QualifiedTypeIdentifier<?> type) {
        return scopes.stream().anyMatch(it -> it.canSupply(type));
    }

    @Override
    public void start() {
        scopes.forEach(Scope::start);
    }

    @Override
    public void finish() {
        scopes.forEach(Scope::finish);
    }

    @Override
    public void link(WireRepository wireRepository) {
        scopes.forEach(it -> it.link(wireRepository));
    }
}
