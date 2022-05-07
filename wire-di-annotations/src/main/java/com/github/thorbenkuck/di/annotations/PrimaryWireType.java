package com.github.thorbenkuck.di.annotations;

import java.lang.annotation.*;

/**
 * Allows you to set the IdentifiableProvider field to what it is responsible for.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Inherited
public @interface PrimaryWireType {

    Class<?> value();

}
