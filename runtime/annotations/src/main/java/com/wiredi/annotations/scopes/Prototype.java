package com.wiredi.annotations.scopes;

import jakarta.inject.Scope;

import java.lang.annotation.*;

/**
 * An annotation that marks a bean as "prototype".
 * <p>
 * A prototype bean is recreated for each injection point.
 * So if a bean is annotated with {@literal @}Prototype, it will be recreated for each injection point.
 * This means that the following code will not fail:
 *
 * <pre>{@code
 * @Wire
 * @Prototype
 * class MyDependency {}
 *
 * @Wire
 * class MyService {
 *     public MyService(MyDependency dependency1, MyDependency dependency2) {
 *         assert dependency1 != dependency2;
 *     }
 * }
 * }</pre>
 *
 * Interpretation is in the hands of the DI container.
 */
@Scope
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@ScopeMetadata(scopeInitializer = "() -> new com.wiredi.runtime.domain.scopes.PrototypeScope()")
public @interface Prototype {
}

