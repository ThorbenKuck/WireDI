package com.wiredi.processor.tck.domain.transactional;

import com.wiredi.annotations.aspects.AspectTarget;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@Inherited
@Documented
@AspectTarget
public @interface Transactional {
}
