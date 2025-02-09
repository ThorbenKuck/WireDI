package com.wiredi.annotations.documentation;

import java.lang.annotation.*;

/**
 * An annotation that describes what is generated as a result of the annotated element.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Generates {
    String[] classes() default {};
    String[] byAnnotationProcessors() default {};
}
