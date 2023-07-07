package com.wiredi.annotations.properties;

import java.lang.annotation.*;

/**
 * This annotation is used to bind properties found in a ".properties" file, to a class.
 *
 * If no file is specified via the {@link #file()} field, the properties will be taken from the TypedProperties that
 * are maintained inside the WireRepository the class is requested from.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Inherited
public @interface Properties {

    /**
     * A prefix, to reduce the properties based on a prefix. All key names determined by fields and parameters are
     * prepended with the here provided prefix.
     *
     * If the prefix does not end with a ".", it will be appended.
     *
     * @return the property prefix for all keys of the annotated class
     */
    String prefix() default "";

    String file() default "";

    Lifecycle lifecycle() default Lifecycle.RUNTIME;

    enum Lifecycle {
        COMPILE,
        RUNTIME
    }
}
