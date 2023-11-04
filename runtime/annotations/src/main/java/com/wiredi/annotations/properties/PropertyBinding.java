package com.wiredi.annotations.properties;

import java.lang.annotation.*;

/**
 * This annotation is used to bind properties found in a "properties" file, to a class.
 * <p>
 * If no file is specified via the {@link #file()} field, the properties will be taken from the TypedProperties that
 * are maintained inside the WireRepository the class is requested from.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Inherited
public @interface PropertyBinding {

    /**
     * A prefix, to reduce the properties based on a prefix. All key names determined by fields and parameters are
     * prepended with the here provided prefix.
     * <p>
     * If the prefix does not end with a ".", it will be appended.
     *
     * @return the property prefix for all keys of the annotated class
     */
    String prefix() default "";

    /**
     * A file which to use for the property bindings.
     * <p>
     * If set, the bound class will ignore the WireRepository properties and take only from the properties located in
     * this provided file.
     * <p>
     * Supports different source protocols (classpath, file, ...).
     *
     * @return the file from which the properties should be taken (if present).
     */
    String file() default "";

    Lifecycle lifecycle() default Lifecycle.RUNTIME;

    enum Lifecycle {
        COMPILE,
        RUNTIME
    }
}
