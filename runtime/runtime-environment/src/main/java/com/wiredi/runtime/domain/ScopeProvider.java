package com.wiredi.runtime.domain;

import com.wiredi.runtime.domain.scopes.JoinedScopeProvider;
import com.wiredi.runtime.domain.scopes.SimpleScopeProvider;
import com.wiredi.runtime.domain.scopes.provider.AnnotationBasedScopeProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;

public interface ScopeProvider {

    static <T extends Annotation> AnnotationBasedScopeProvider.Builder<T> forAnnotation(Class<T> type) {
        return new AnnotationBasedScopeProvider.Builder<>(type);
    }

    static SimpleScopeProvider.Builder forIdentifier(Object identifier) {
        return new SimpleScopeProvider.Builder(identifier);
    }

    @Nullable
    Scope getScope(@NotNull ScopeRegistry registry);

    default ScopeProvider and(ScopeProvider provider) {
        return new JoinedScopeProvider(this, provider);
    }
}
