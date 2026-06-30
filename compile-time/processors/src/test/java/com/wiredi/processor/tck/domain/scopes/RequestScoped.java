package com.wiredi.processor.tck.domain.scopes;

import com.wiredi.annotations.scopes.ScopeMetadata;
import jakarta.inject.Scope;

@Scope
@ScopeMetadata(scopeProvider = RequestScopeProvider.class)
public @interface RequestScoped {
}
