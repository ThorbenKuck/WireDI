package com.wiredi.annotations;

import java.lang.annotation.*;

/**
 * TODO: Make me work in issue #3
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
@Documented
@Inherited
public @interface LifecycleScope {

	Scope value();

	enum Scope {
		RUNTIME,
		COMPILE_TIME
	}
}
