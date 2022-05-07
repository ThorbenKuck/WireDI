package com.github.thorbenkuck.di.annotations.aspects;

import java.lang.annotation.*;

/**
 * Marks an annotated class as a Facet, which  may hold any amount of Aspects.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Inherited
public @interface Facet {
}
