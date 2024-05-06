package com.wiredi.aop;

import com.wiredi.annotations.aspects.AspectTarget;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@Inherited
@Documented
@AspectTarget
public @interface Transactional {
}
