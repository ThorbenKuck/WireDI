package com.wiredi.annotations;

import java.lang.annotation.*;

/**
 * Allows you to set the IdentifiableProvider field to what it is responsible for.
 * <p>
 * This annotation is used to override what the annotation processor generates for the "type" field in the IdentifiableProvider.
 * If absent, the annotation processor uses the class of the {@link Wire} annotation.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Inherited
public @interface PrimaryWireType {

    /**
     * The type to use for the IdentifiableProvider.
     *
     * @return the type to use
     */
    Class<?> value();

}
