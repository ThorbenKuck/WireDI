package com.wiredi.annotations.stereotypes;

import com.wiredi.annotations.Order;
import com.wiredi.annotations.Wire;

import java.lang.annotation.*;

/**
 * A special configuration.
 * <p>
 * AutoConfigurations are generally executed before normal configurations are applied.
 * This is achieved by using an {@link Order} with a lower value.
 * <p>
 * This annotation is just a stereotype.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Wire(proxy = false)
@Order(Order.AUTO_CONFIGURATION)
public @interface AutoConfiguration {
}
