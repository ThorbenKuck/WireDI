package com.wiredi.annotations.properties;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Inherited
@Documented
public @interface Property {

    /**
     * The name of the Property.
     * <p>
     * In the context of a {@link PropertyBinding}, this specifies the suffix of the property.
     * It is constructed via {@link PropertyBinding#prefix()} + this name.
     * <p>
     * If the annotation is used as on an injection candidate, the name is expected to be the fully qualified name.
     *
     * @return the property name to access
     */
    String name() default "";

    /**
     * A default value for this injection.
     * <p>
     * If, during injection, there is no value for this property present, the default value is supposed to be used.
     *
     * @return the default value for this property
     */
    String defaultValue() default "";

    /**
     * The description to use in the generated metadata.
     *
     * @return the description
     */
    String description() default "";

}
