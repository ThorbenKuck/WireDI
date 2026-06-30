package com.wiredi.processor.tck.domain.scopes;

import com.wiredi.runtime.domain.scopes.SimpleScopeProvider;
import com.wiredi.runtime.domain.scopes.SingletonScope;

public class RequestScopeProvider extends SimpleScopeProvider {
    public RequestScopeProvider() {
        super(RequestScoped.class, SingletonScope::new);
    }
}
