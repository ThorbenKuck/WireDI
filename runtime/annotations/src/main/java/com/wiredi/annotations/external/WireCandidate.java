package com.wiredi.annotations.external;

import com.wiredi.annotations.Wire;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Repeatable(WireConfiguration.class)
@Documented
@Inherited
public @interface WireCandidate {
	Class<?> value();

	Wire wire() default @Wire;
}
