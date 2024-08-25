package com.wiredi.runtime.messaging.annotations;

import java.lang.annotation.*;

@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Header {

    /**
     * The name of the header to resolve.
     * <p>
     * If empty, the name of the parameter will be used.
     *
     * @return the name of the header
     */
    String name() default "";

}
