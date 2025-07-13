package com.wiredi.annotations.scopes;

import jakarta.inject.Scope;

import java.lang.annotation.*;

@Scope
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@ScopeMetadata(scopeInitializer = "() -> new com.wiredi.runtime.domain.scopes.PrototypeScope(true)")
public @interface Prototype {
}

