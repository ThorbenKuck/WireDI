package com.wiredi.annotations.stereotypes;

import com.wiredi.annotations.Order;
import com.wiredi.annotations.Wire;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Wire(proxy = false)
@Order(Order.AUTO_CONFIGURATION)
public @interface AutoConfiguration {
}
