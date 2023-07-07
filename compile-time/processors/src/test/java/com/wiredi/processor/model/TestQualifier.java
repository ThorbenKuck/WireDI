package com.wiredi.processor.model;

import jakarta.inject.Qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Qualifier
public @interface TestQualifier {

	int value();

	String scope() default "TEST";

}
