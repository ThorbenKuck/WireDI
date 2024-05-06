package com.wiredi.annotations;

import java.lang.annotation.*;

/**
 * An annotation to either set the order of a class or a {@link Provider} function.
 * <p>
 * A lower order has a higher precedence.
 * <p>
 * This annotation is used as metadata for the Ordered interface of the runtime-environment.
 * When you annotate a Wire candidate with {@literal @}Order, it is expected to implement this interface.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Order {

    /**
     * The last entry in the order.
     */
    int LAST = Integer.MAX_VALUE;

    /**
     * The first entry in the order.
     */
    int FIRST = Integer.MIN_VALUE;

    /**
     * The default value for all ordered classes.
     */
    int DEFAULT = 0;

    /**
     * A value commonly used for auto configurations.
     * <p>
     * Auto configurations happen before custom configurations and commonly use
     * conditions to disable after custom configuration are applied.
     */
    int AUTO_CONFIGURATION = -100;

    /**
     * The order value for the annotated element (i.e method, or class).
     *
     * <p>Elements are ordered based on priority where a lower value has greater
     * priority than a higher value. In an ordered Collection, the first element
     * will be the one element with the lowest value, whilst the last element
     * is the element with the greatest value.
     *
     * @see #DEFAULT
     * @see #FIRST
     * @see #LAST
     */
    int value() default DEFAULT;

    /**
     * If set, the order of the annotated class will be the order of the referenced class - 1.
     * <p>
     * If the referenced class is not annotated with @Order, the {@link Order#DEFAULT} order will be taken.
     * <p>
     * Use this in combination with {@link #after()} with great care!
     * If after is set as well, the following must be true:
     * <ul>
     *     <li>after must resolve to a lower order than before</li>
     *     <li>the difference between before an after must be at least 2!</li>
     * </ul>
     * If both are set, the order of this class will resolve to: <pre>after - Math.floor((before-after) / 2)</pre>
     * <p>
     * Let's say that after resolves to order 5, and before is 7.
     * In this example, the order of the annotated class will be resolved to 6.
     * If before and after do not have a difference of at least 2,
     * no concrete order of the annotated class can be resolved!
     *
     * <h2>Note</h2>
     * This field only takes effect in annotation processors.
     * It is not used in the related {@code OrderedComparator}, as we want to remove reflection checks at runtime.
     *
     * @return a class which should have a higher order than the annotated class.
     */
    Class<?> before() default Void.class;

    /**
     * If set, the order of the annotated class will be the order of referenced class + 1.
     * <p>
     * If the referenced class is not annotated with @Order, the {@link Order#DEFAULT} order will be taken.
     * <p>
     * Use this in combination with {@link #before()} with great care!
     * If after is set as well, the following must be true:
     * <ul>
     *     <li>after must resolve to a lower order than before</li>
     *     <li>the difference between before an after must be at least 2!</li>
     * </ul>
     * If both are set, the order of this class will resolve to: <pre>after - Math.floor((before-after) / 2)</pre>
     * <p>
     * Let's say, that after resolves to the order 5, and before to 7.
     * In this example, the order of the annotated class will be resolved to 6.
     * If before and after do not have a difference of at least 2,
     * no concrete order of the annotated class can be resolved!
     *
     * <h2>Note</h2>
     * This field only takes effect in annotation processors.
     * It is not used in the related {@code OrderedComparator}, as we want to remove reflection checks at runtime.
     *
     * @return a class which should have a lower order than the annotated class.
     */
    Class<?> after() default Void.class;

}