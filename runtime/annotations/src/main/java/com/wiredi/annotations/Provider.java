package com.wiredi.annotations;

import com.wiredi.annotations.aspects.Pure;

import java.lang.annotation.*;

/**
 * This annotation marks a method as a factory for a wired component.
 * <p>
 * The annotated method must have a return value other than void and thereby marks it as a provider for the return
 * value. It is required that the annotated method does not return void.
 * <p>
 * This might look like this:
 *
 * <pre><code>@Wire
 * public class Example {
 *     {@literal @Provider}
 *     public Value createValue(Dependency dependency) {
 *          // create the value
 *          return new Value(dependency);
 *     }
 * }</code></pre>
 * <p>
 * This mean that, when any wired component requests the "Value" dependency, there will be an IdentifiableProvider,
 * which will request the Example class and call the method "createValue".
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@Documented
@Inherited
public @interface Provider {

    boolean singleton() default true;

    /**
     * Whether the provided class should be wired to all super types or not.
     * <p>
     * If true, the provided class will be wired to all interfaces and super classes,
     * as well as all interfaces and super class of the super types.
     * <p>
     * The provided class will not be wired to the super type Object.
     * <p>
     * Setting this to false will only wire the returned instance to the concrete return type of the provider
     * method and not to any super type.
     *
     * @return whether the provided instance should be wired to all super types
     */
    SuperTypes respect() default SuperTypes.ALL;

    enum SuperTypes {
        /**
         * This will wire the instance returned by the provider method to all super types.
         * <p>
         * This includes all interfaces and abstract classes, not limited to declared.
         */
        ALL,

        /**
         * This will wire the instance returned by the provider method only to declared super types of the returned value.
         * <p>
         * Concrete this means that the returned instance is wired to the return type, as well as any interface and
         * abstract classes of this type, but no interface of interface or abstract classes of abstract classes.
         * Only direct members are respected.
         */
        DECLARED,

        /**
         * This will wire the instance returned by the provider method only to the return value of the method, ignoring
         * any interface and abstract classes
         */
        NONE;
    }
}
