package com.github.thorbenkuck.di.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Inherited
public @interface PrimaryWireType {

    Class<?> value();

}
