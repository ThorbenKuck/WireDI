package com.github.thorbenkuck.di.test;

import com.github.thorbenkuck.di.annotations.aspects.AspectTarget;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@AspectTarget
public @interface PrintParameter {
}
