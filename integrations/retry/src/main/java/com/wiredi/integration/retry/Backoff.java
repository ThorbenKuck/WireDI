package com.wiredi.integration.retry;

import com.wiredi.runtime.domain.annotations.AnnotationMetadata;

import java.util.concurrent.TimeUnit;

public @interface Backoff {

    AnnotationMetadata DEFAULT_META_DATA = AnnotationMetadata.builder("Backoff")
            .withField("multiplier", 0.0)
            .withField("value", 0L)
            .withEnum("backoffUnit", TimeUnit.MILLISECONDS)
            .build();

    TimeUnit backoffUnit = TimeUnit.MILLISECONDS;

    double multiplier() default 1.0;

    long value() default 0;

}
