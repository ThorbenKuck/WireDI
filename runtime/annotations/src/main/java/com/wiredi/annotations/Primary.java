package com.wiredi.annotations;

import java.lang.annotation.*;

/**
 * Marks either a class or provider function, as a primary wire type.
 * <p>
 * Primary means that if the context contains multiple unqualified beans,
 * the primary instance will be injected if requested.
 * <p>
 * If multiple beans are marked as primary, the injector should fail on injection.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
@Inherited
public @interface Primary {
}
