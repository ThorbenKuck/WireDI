package com.wiredi.runtime.domain.scopes;

import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.domain.Scope;
import com.wiredi.runtime.domain.ScopeCallback;
import com.wiredi.runtime.domain.factories.Bean;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.QualifiedTypeIdentifier;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.exceptions.MissingBeanException;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class UnionScope implements Scope {

    private static final Collector<? super Scope, ?, UnionScope> COLLECTOR = Collector.of(
            UnionScope::new,
            UnionScope::addScope,
            (left, right) -> {
                left.addScopes(right);
                return left;
            }
    );
    @NotNull
    private final Set<@NotNull Scope> scopes = new HashSet<>();

    public UnionScope(@NotNull Collection<@NotNull Scope> scopes) {
        this.scopes.addAll(scopes);
    }

    public UnionScope() {
    }

    public static Collector<? super Scope, ?, UnionScope> collector() {
        return COLLECTOR;
    }

    public void clearScopes() {
        scopes.clear();
    }

    public boolean containsScope(@NotNull Predicate<@NotNull Scope> scopePredicate) {
        return scopes.stream().anyMatch(scopePredicate);
    }

    public @NotNull Optional<Scope> findScope(@NotNull Predicate<@NotNull Scope> scopePredicate) {
        return scopes.stream().filter(scopePredicate).findFirst();
    }

    public boolean addScope(@NotNull Scope scope) {
        return scopes.add(scope);
    }

    public boolean addScopes(@NotNull Collection<Scope> scopes) {
        return this.scopes.addAll(scopes);
    }

    public boolean addScopes(UnionScope unionScope) {
        return this.scopes.addAll(unionScope.scopes);
    }

    @Override
    public <T> @NotNull Optional<T> tryGet(@NotNull QualifiedTypeIdentifier<T> qualifierType) {
        for (Scope scope : scopes) {
            Optional<T> optional = scope.tryGet(qualifierType);
            if (optional.isPresent()) {
                return optional;
            }
        }

        return Optional.empty();
    }

    @Override
    public @NotNull <T> Optional<T> tryGet(@NotNull TypeIdentifier<T> qualifierType) {
        for (Scope scope : scopes) {
            Optional<T> optional = scope.tryGet(qualifierType);
            if (optional.isPresent()) {
                return optional;
            }
        }

        return Optional.empty();
    }

    @Override
    public @NotNull <T> IdentifiableProvider<T> getProvider(@NotNull TypeIdentifier<T> qualifierType) {
        for (Scope scope : scopes) {
            try {
                return scope.getProvider(qualifierType);
            } catch (MissingBeanException ignored) {
                // Ignore
            }
        }

        throw MissingBeanException.unableToCreate(qualifierType);
    }

    @Override
    public <T> @NotNull IdentifiableProvider<T> getProvider(@NotNull QualifiedTypeIdentifier<T> qualifierType) {
        for (Scope scope : scopes) {
            try {
                return scope.getProvider(qualifierType);
            } catch (MissingBeanException ignored) {
                // Ignore
            }
        }

        throw MissingBeanException.unableToCreate(qualifierType.type());
    }

    @Override
    public @NotNull <T> T get(@NotNull TypeIdentifier<T> qualifierType) {
        for (Scope scope : scopes) {
            Optional<T> optional = scope.tryGet(qualifierType);
            if (optional.isPresent()) {
                return optional.get();
            }
        }

        throw MissingBeanException.unableToCreate(qualifierType);
    }

    @Override
    public <T> @NotNull T get(@NotNull QualifiedTypeIdentifier<T> qualifierType) {
        for (Scope scope : scopes) {
            Optional<T> optional = scope.tryGet(qualifierType);
            if (optional.isPresent()) {
                return optional.get();
            }
        }

        throw MissingBeanException.unableToCreate(qualifierType.type());
    }

    @Override
    public @NotNull <T> Stream<Bean<T>> getAllBeans(@NotNull TypeIdentifier<T> type) {
        return scopes.stream().flatMap(scope -> scope.getAllBeans(type));
    }

    @Override
    public @NotNull <T> List<T> getAll(@NotNull TypeIdentifier<T> type) {
        return scopes.stream().flatMap(scope -> scope.getAll(type).stream()).toList();
    }

    @Override
    public boolean contains(@NotNull QualifiedTypeIdentifier<?> type) {
        return scopes.stream()
                .anyMatch(it -> it.contains(type));
    }

    @Override
    public boolean contains(@NotNull TypeIdentifier<?> type) {
        return scopes.stream().anyMatch(it -> it.contains(type));
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
    public boolean canSupply(@NotNull TypeIdentifier<?> type) {
        return scopes.stream().anyMatch(it -> it.canSupply(type));
    }

    @Override
    public void reset() {
        scopes.forEach(Scope::reset);
    }

    @Override
    public void link(@NotNull WireContainer wireContainer) {
        scopes.forEach(it -> it.link(wireContainer));
    }

    @Override
    public void callback(@NotNull ScopeCallback scopeCallback) {
        scopes.forEach(it -> it.callback(scopeCallback));
    }

    @Override
    public void unlink() {
        scopes.forEach(Scope::unlink);
    }

    @Override
    public String toString() {
        return "UnionScope{" + scopes + '}';
    }
}
