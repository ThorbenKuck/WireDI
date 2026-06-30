package com.wiredi.compiler.domain;

import com.wiredi.annotations.scopes.ScopeMetadata;
import com.wiredi.logging.Logging;
import com.wiredi.runtime.domain.annotations.AnnotationMetadata;
import com.wiredi.runtime.scope.ScopeType;
import jakarta.inject.Scope;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import java.util.List;
import java.util.Optional;

public class Scopes {
    public List<ScopeType> allScopesOf(Element element) {
        return Annotations.search()
                .metaAnnotatedWith(Scope.class)
                .findAllMirrorsIn(element)
                .stream()
                .map(it -> {
                    ScopeType scopeType = ScopeType.of(it);
                    Optional<AnnotationMirror> metadata = Annotations.search().byType(ScopeMetadata.class).findFirstMirrorIn(it);
                    if (metadata.isPresent()) {
                        scopeType = scopeType.withMetadata(metadata.get());
                    }
                    return scopeType;
                })
                .toList();
    }
}
