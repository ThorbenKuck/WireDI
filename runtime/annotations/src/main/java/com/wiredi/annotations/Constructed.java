package com.wiredi.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
@Documented
public @interface Constructed {

	boolean async() default true;

}
