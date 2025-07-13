package com.wiredi.runtime.domain.scopes.exceptions;

import com.wiredi.runtime.domain.Scope;

public class ScopeNotActivatedException extends RuntimeException {

    private final Scope scope;

    public ScopeNotActivatedException(Scope scope) {
        this(scope, "Tried to access inactive scope " + scope);
    }

    public ScopeNotActivatedException(Scope scope, String message) {
        super(message);
        this.scope = scope;
    }

    public Scope getScope() {
        return scope;
    }
}
