package com.wiredi.test;

import com.wiredi.annotations.aspects.AspectTarget;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@AspectTarget(aspect = CountInvocationsAspect.class)
public @interface CountInvocations {
}
