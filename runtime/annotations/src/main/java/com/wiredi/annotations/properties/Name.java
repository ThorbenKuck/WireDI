package com.wiredi.annotations.properties;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Inherited
@Documented
public @interface Name {

	String value();

}
