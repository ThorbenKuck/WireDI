package com.wiredi.inherited;

import com.wiredi.annotations.Order;
import com.wiredi.annotations.Wire;
import com.wiredi.annotations.stereotypes.Configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Configuration
@Order(101)
public @interface ExampleConfiguration {
}
