package com.wiredi.annotations.properties;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Inherited
public @interface PropertySource {

    /**
     * The files to load into the WireRepository properties.
     * <p>
     * If not empty, all files will be loaded in the generated EnvironmentConfiguration
     * <p>
     * Supports different source protocols (classpath, file, ...).
     *
     * @return all property files that should be loaded into the WireRepository properties.
     */
    String[] value() default {};

    Entry[] entries() default {};
}
