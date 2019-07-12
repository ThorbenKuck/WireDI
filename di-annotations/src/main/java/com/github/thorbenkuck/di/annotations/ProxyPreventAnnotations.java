package com.github.thorbenkuck.di.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface ProxyPreventAnnotations {

	// TODO Implement in annotation processor
	Class<? extends Annotation>[] value() default {};

}
