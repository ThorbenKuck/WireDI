package com.wiredi.integration.retry;

import com.wiredi.runtime.domain.AnnotationMetaData;

import java.util.concurrent.TimeUnit;

public @interface Backoff {

    AnnotationMetaData DEFAULT_META_DATA = AnnotationMetaData.newInstance("Backoff")
            .withField("multiplier", 0.0)
            .withField("value", 0L)
            .withEnum("backoffUnit", TimeUnit.MILLISECONDS)
            .build();

    TimeUnit backoffUnit = TimeUnit.MILLISECONDS;

    double multiplier() default 1.0;

    long value() default 0;

}
