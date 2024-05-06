package com.wiredi.integration.retry;

import com.wiredi.annotations.aspects.AspectTarget;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
@AspectTarget
public @interface Retry {

    /**
     * The max amount that the execution can be retried.
     * <p>
     * This results in the max amount of times the code is executed to be max retries + 1;
     * A value of less than 1 will result in an indefinite retry.
     *
     * @return the max amount that the execution can be retried.
     */
    long maxRetries() default -1;

    long maxTimeout() default -1;

    TimeUnit maxTimeoutUnit() default TimeUnit.MILLISECONDS;

    Backoff backoff() default @Backoff();
}
