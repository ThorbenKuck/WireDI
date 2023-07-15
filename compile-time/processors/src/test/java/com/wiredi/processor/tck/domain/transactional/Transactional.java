package com.wiredi.processor.tck.domain.transactional;

import com.wiredi.annotations.aspects.AspectTarget;
import kotlin.annotation.MustBeDocumented;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@Inherited
@MustBeDocumented
@AspectTarget
public @interface Transactional {
}
