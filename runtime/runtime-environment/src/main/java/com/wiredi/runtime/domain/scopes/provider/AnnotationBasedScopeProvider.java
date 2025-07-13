package com.wiredi.runtime.domain.scopes.provider;

import com.wiredi.runtime.domain.Scope;
import com.wiredi.runtime.domain.ScopeProvider;
import com.wiredi.runtime.domain.ScopeRegistry;
import com.wiredi.runtime.domain.scopes.SingletonScope;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

public class AnnotationBasedScopeProvider<T extends Annotation> implements ScopeProvider {

    private final Class<T> annotationClass;
    private final Supplier<Scope> scopeSupplier;

    public AnnotationBasedScopeProvider(Class<T> annotationClass) {
        this(annotationClass, () -> new SingletonScope(false));
    }

    public AnnotationBasedScopeProvider(Class<T> annotationClass, Supplier<Scope> scopeSupplier) {
        this.annotationClass = annotationClass;
        this.scopeSupplier = scopeSupplier;
    }

    @Override
    public @NotNull Scope getScope(@NotNull ScopeRegistry registry) {
        return registry.registerIfAbsent(annotationClass, scopeSupplier);
    }

    public static class Builder<T extends Annotation> {

        @NotNull
        private final Class<T> scopeAnnotation;
        @NotNull
        private Supplier<@NotNull Scope> scopeSupplier = () -> new SingletonScope(false);

        public Builder(@NotNull Class<T> scopeAnnotation) {
            this.scopeAnnotation = scopeAnnotation;
        }

        @NotNull
        public Builder<T> withScope(@NotNull Scope scope) {
            this.scopeSupplier = () -> scope;
            return this;
        }

        @NotNull
        public Builder<T> withScope(@NotNull Supplier<@NotNull Scope> supplier) {
            this.scopeSupplier = supplier;
            return this;
        }

        public AnnotationBasedScopeProvider<T> build() {
            return new AnnotationBasedScopeProvider<>(scopeAnnotation, scopeSupplier);
        }
    }
}
