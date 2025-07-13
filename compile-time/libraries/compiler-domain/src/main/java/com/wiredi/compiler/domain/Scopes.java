package com.wiredi.compiler.domain;

import com.wiredi.annotations.scopes.ScopeMetadata;
import com.wiredi.runtime.scope.ScopeType;
import jakarta.inject.Scope;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import java.util.List;
import java.util.Optional;

public class Scopes {

    private final Annotations annotations;

    public Scopes(Annotations annotations) {
        this.annotations = annotations;
    }

    public List<ScopeType> allScopesOf(Element element) {
        return Annotations.findAll(element, it -> Annotations.isAnnotatedWith(it, Scope.class))
                .map(it -> {
                    ScopeType scopeType = ScopeType.of(it);
                    Optional<AnnotationMirror> metadata = annotations.findAnnotationMirror(it.getAnnotationType().asElement(), ScopeMetadata.class);
                    if (metadata.isPresent()) {
                        scopeType = scopeType.withMetadata(metadata.get());
                    }

                    return scopeType;
                })
                .toList();
    }
}
