package com.wiredi.annotations;

import java.lang.annotation.*;

/**
 * Marks either a class or provider function, as a primary wire type.
 * <p>
 * Primary means, that if the context contains multiple unqualified injection beans,
 * the primary classes
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
@Inherited
public @interface Primary {
}
