package com.wiredi.annotations.scopes;

import java.lang.annotation.*;

/**
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface ScopeMetadata {

    String scopeInitializer() default "";

    Class<?> scopeProvider() default Void.class;

}
