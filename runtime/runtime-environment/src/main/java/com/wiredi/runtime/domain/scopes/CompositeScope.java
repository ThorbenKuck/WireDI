package com.wiredi.runtime.domain.scopes;

import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.domain.Scope;
import com.wiredi.runtime.domain.factories.Bean;
import com.wiredi.runtime.domain.factories.MissingBeanException;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.QualifiedTypeIdentifier;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.domain.scopes.exceptions.ScopeNotActivatedException;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class CompositeScope implements Scope {

    private static final Collector<? super Scope, ?, CompositeScope> COLLECTOR = Collector.of(
            CompositeScope::new,
            CompositeScope::addScope,
            (left, right) -> {
                left.addScopes(right);
                return left;
            }
    );
    @NotNull
    private final List<@NotNull Scope> scopes = new ArrayList<>();
    private volatile List<Scope> activeScopes = new ArrayList<>();
    private boolean isActive = false;

    public CompositeScope(@NotNull Collection<@NotNull Scope> scopes) {
        this.scopes.addAll(scopes);
    }

    public CompositeScope() {
    }

    public static Collector<? super Scope, ?, CompositeScope> collector() {
        return COLLECTOR;
    }

    private void updateActiveScopes() {
        activeScopes = scopes.stream()
                .filter(Scope::isActive)
                .toList();
    }

    public void clearScopes() {
        scopes.clear();
        activeScopes = Collections.emptyList();
    }

    public boolean containsScope(@NotNull Predicate<@NotNull Scope> scopePredicate) {
        return scopes.stream().anyMatch(scopePredicate);
    }

    public @NotNull Optional<Scope> findScope(@NotNull Predicate<@NotNull Scope> scopePredicate) {
        return scopes.stream().filter(scopePredicate).findFirst();
    }

    public void addScope(@NotNull Scope scope) {
        scopes.add(scope);
        if (isActive) {
            updateActiveScopes();
        }
    }

    public void addScopes(@NotNull Collection<Scope> scopes) {
        this.scopes.addAll(scopes);
        if (isActive) {
            updateActiveScopes();
        }
    }

    public void addScopes(CompositeScope compositeScope) {
        this.scopes.addAll(compositeScope.scopes);
        if (isActive) {
            updateActiveScopes();
        }
    }

    @Override
    public <T> @NotNull Optional<T> tryGet(@NotNull QualifiedTypeIdentifier<T> qualifierType) {
        if (!isActive) {
            return Optional.empty();
        }

        for (Scope scope : activeScopes) {
            Optional<T> optional = scope.tryGet(qualifierType);
            if (optional.isPresent()) {
                return optional;
            }
        }

        return Optional.empty();
    }

    @Override
    public @NotNull <T> Optional<T> tryGet(@NotNull TypeIdentifier<T> typeIdentifier) {
        if (!isActive) {
            return Optional.empty();
        }

        for (Scope scope : activeScopes) {
            Optional<T> optional = scope.tryGet(typeIdentifier);
            if (optional.isPresent()) {
                return optional;
            }
        }

        return Optional.empty();

    }

    @Override
    public @NotNull <T> IdentifiableProvider<T> getProvider(@NotNull TypeIdentifier<T> typeIdentifier) {
        checkActive();
        for (Scope scope : scopes) {
            return scope.getProvider(typeIdentifier);
        }

        throw MissingBeanException.unableToCreate(typeIdentifier);

    }

    @Override
    public <T> @NotNull IdentifiableProvider<T> getProvider(@NotNull QualifiedTypeIdentifier<T> qualifierType) {
        checkActive();
        for (Scope scope : scopes) {
            return scope.getProvider(qualifierType);
        }

        throw MissingBeanException.unableToCreate(qualifierType.type());
    }

    @Override
    public <T> @NotNull T get(@NotNull TypeIdentifier<T> qualifierType) {
        checkActive();
        for (Scope scope : activeScopes) {
            if (scope.canSupply(qualifierType)) {
                return scope.get(qualifierType);
            }
        }

        throw MissingBeanException.unableToCreate(qualifierType);

    }

    @Override
    public <T> @NotNull T get(@NotNull QualifiedTypeIdentifier<T> qualifierType) {
        checkActive();
        for (Scope scope : activeScopes) {
            if (scope.canSupply(qualifierType)) {
                return scope.get(qualifierType);
            }
        }

        throw MissingBeanException.unableToCreate(qualifierType.type());
    }

    @Override
    public @NotNull <T> Stream<Bean<T>> getAllBeans(@NotNull TypeIdentifier<T> type) {
        return activeScopes.stream()
                .flatMap(it -> it.getAllBeans(type));
    }

    @Override
    public boolean contains(@NotNull TypeIdentifier<?> type) {
        return activeScopes.stream().anyMatch(it -> it.contains(type));
    }

    @Override
    public boolean contains(@NotNull QualifiedTypeIdentifier<?> type) {
        return activeScopes.stream().anyMatch(it -> it.contains(type));
    }

    @Override
    public <T> @NotNull List<T> getAll(@NotNull TypeIdentifier<T> type) {
        if (activeScopes.isEmpty()) return Collections.emptyList();

        // Optimize for single scope case
        if (activeScopes.size() == 1) {
            return activeScopes.getFirst().getAll(type);
        }

        // Use ArrayList instead of Set to avoid hashCode overhead
        List<T> result = new ArrayList<>();
        for (Scope scope : activeScopes) {
            result.addAll(scope.getAll(type));
        }
        return result;
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
    public void start() {
        scopes.forEach(Scope::start);
        this.isActive = true;
        updateActiveScopes();
    }

    @Override
    public void autostart() {
        scopes.forEach(Scope::autostart);
        this.isActive = true;
        updateActiveScopes();
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void finish() {
        scopes.forEach(Scope::finish);
        this.isActive = false;
    }

    @Override
    public void link(WireContainer wireContainer) {
        scopes.forEach(it -> it.link(wireContainer));
    }

    @Override
    public void unlink() {
        scopes.forEach(Scope::unlink);
    }

    private void checkActive() {
        if (!isActive) {
            throw new ScopeNotActivatedException(this);
        }
    }

    public String toString() {
        return "CompositeScope{active=" + isActive + ", scopes=" + scopes + '}';
    }
}
