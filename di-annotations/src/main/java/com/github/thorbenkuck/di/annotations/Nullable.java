package com.github.thorbenkuck.di.annotations;

import java.lang.annotation.*;

/**
 * This annotation allows injections to be null.
 *
 * Either field or construct injection points have to be not null by default, unless you explicitly declare them as
 * nullable using this specific annotation.
 *
 * Every other time, if you try to inject null, you will get an Exception at Runtime.
 *
 * This behaviour is due to the fact, that this is a simple example project. If you want a more powerful di framework,
 * i would love to recommend you Dagger.
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.CLASS)
public @interface Nullable {
}
