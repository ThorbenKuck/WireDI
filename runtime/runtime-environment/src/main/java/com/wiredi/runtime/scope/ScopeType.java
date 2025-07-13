package com.wiredi.runtime.scope;

import com.wiredi.runtime.domain.annotations.AnnotationMetadata;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.AnnotationMirror;
import java.util.Optional;

public record ScopeType(
        @NotNull AnnotationMirror scopeAnnotation,
        @Nullable Metadata metadata
) {
    @NotNull
    public static ScopeType of(@NotNull AnnotationMirror annotationMirror) {
        return new ScopeType(annotationMirror, null);
    }

    @NotNull
    public ScopeType withMetadata(@NotNull Metadata metadata) {
        return new ScopeType(scopeAnnotation, metadata);
    }

    @NotNull
    public ScopeType withMetadata(@NotNull AnnotationMirror annotationMirror) {
        AnnotationMetadata annotationMetadata = AnnotationMetadata.of(annotationMirror);
        Optional<String> initializer = annotationMetadata.get("scopeInitializer");
        Optional<TypeIdentifier<?>> scopeProvider = annotationMetadata.getType("scopeProvider");
        if (initializer.isEmpty() && scopeProvider.isEmpty()) {
            return this;
        }
        return new ScopeType(scopeAnnotation, new Metadata(initializer.orElse(null), scopeProvider.orElse(null)));
    }

    public record Metadata(
            @Nullable String scopeInitializer,
            @Nullable TypeIdentifier<?> scopeProvider
    ) {
        public Metadata {
            if (scopeInitializer != null && scopeProvider != null) {
                throw new IllegalArgumentException("The scope initializer and scope provider cannot be both set");
            }
        }
    }
}
