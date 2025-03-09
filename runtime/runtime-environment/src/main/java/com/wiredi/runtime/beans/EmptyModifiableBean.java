package com.wiredi.runtime.beans;

import com.wiredi.runtime.beans.value.BeanValue;
import com.wiredi.runtime.beans.value.BeanValueError;
import com.wiredi.runtime.domain.WireConflictResolver;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.qualifier.QualifierType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class EmptyModifiableBean extends ModifiableBean<Void> {
    private EmptyModifiableBean() {
        super(TypeIdentifier.of(Void.class));
    }

    public static final EmptyModifiableBean INSTANCE = new EmptyModifiableBean();

    @Override
    public void register(TypeIdentifier<Void> concreteType, IdentifiableProvider<Void> identifiableProvider) {
        throw new UnsupportedOperationException("An empty bean cannot register providers");
    }

    @Override
    public void registerPrimaryProvider(TypeIdentifier<Void> concreteType, IdentifiableProvider<Void> identifiableProvider) {
        throw new UnsupportedOperationException("An empty bean cannot register primary providers");
    }

    @Override
    public void addUnqualifiedProvider(TypeIdentifier<Void> concreteType, IdentifiableProvider<Void> identifiableProvider) {
        throw new UnsupportedOperationException("An empty bean cannot register unqualified providers");
    }

    @Override
    public void addQualifiedProvider(IdentifiableProvider<Void> newProvider, List<QualifierType> qualifiers) {
        throw new UnsupportedOperationException("An empty bean cannot register qualified providers");
    }

    @Override
    public @NotNull List<IdentifiableProvider<Void>> getAll() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull List<IdentifiableProvider<Void>> getAllUnqualified() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull List<IdentifiableProvider<Void>> getAllQualified() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull BeanValue<Void> get(QualifierType qualifierType) {
        return BeanValue.empty();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean hasQualifiedProviders() {
        return false;
    }

    @Override
    public boolean hasUnqualifiedProviders() {
        return false;
    }

    @Override
    public boolean hasPrimary() {
        return false;
    }

    @Override
    public @NotNull BeanValue<Void> get(TypeIdentifier<Void> concreteType, Supplier<WireConflictResolver> conflictResolver) {
        return BeanValue.empty();
    }

    @Override
    public int hashCode() {
        return -1;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == INSTANCE;
    }

    @Override
    public String toString() {
        return "EmptyModifiableBean()";
    }
}
