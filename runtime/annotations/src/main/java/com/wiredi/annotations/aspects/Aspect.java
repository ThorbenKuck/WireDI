package com.wiredi.annotations.aspects;

import java.lang.annotation.*;

/**
 * Marks an aspect enabled method, which is found inside a class annotated with {@link Facet {@literal @Facet}}.
 *
 * Methods annotated with this annotation, must fulfill two criteria:
 *
 * <ol>
 *     <li>The class which holds this method must be annotated with {@link Facet {@literal @Facet}}</li>
 *     <li>The sole parameter of the method must be an ExecutionContext with the same generic value as declared in {@link #around()}</li>
 * </ol>
 *
 * Any other scenario should be rejected by the annotation processor.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@Inherited
public @interface Aspect {

    /**
     * The annotation, this aspect is meant to process
     *
     * @return the annotation that triggers this specific aspect
     */
    Class<? extends Annotation> around();

}
