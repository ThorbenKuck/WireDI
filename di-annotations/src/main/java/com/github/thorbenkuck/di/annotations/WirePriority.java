package com.github.thorbenkuck.di.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Documented
public @interface WirePriority {

	int HIGHEST = Integer.MAX_VALUE;

	int LOWEST = Integer.MIN_VALUE;

	int DEFAULT = 0;

	int value();
}

