package com.wiredi.annotations.stereotypes;

import com.wiredi.annotations.Order;
import com.wiredi.annotations.Wire;

import java.lang.annotation.*;

/**
 * A special configuration for default configurations if no other configurations are provided.
 * <p>
 * Default configurations are applied after all other configurations are applied.
 * The goal of default configuration is to provide configuration in the sense of "if not specified otherwise".
 * <p>
 * This is achieved by using an {@link Order} with a higher value, running later.
 * <p>
 * This annotation is just a stereotype.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Wire(proxy = false)
@Order(Order.DEFAULT_CONFIGURATION)
public @interface DefaultConfiguration {
}
