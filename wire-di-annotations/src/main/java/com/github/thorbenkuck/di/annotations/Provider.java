package com.github.thorbenkuck.di.annotations;

import java.lang.annotation.*;

/**
 * This annotation marks a method as a factory for a wired component.
 *
 * The annotated method must have a return value other than void and thereby marks it as a provider for the return
 * value. It is required that the annotated method does not return void.
 *
 * This might look like this:
 *
 * <code><pre>@Wire
 * public class Example {
 *     {@literal @Provider}
 *     public Value createValue(Dependency dependency) {
 *          // create the value
 *          return new Value(dependency);
 *     }
 * }</pre></code>
 *
 * This mean that, when any wired component requests the "Value" dependency, there will be an IdentifiableProvider,
 * which will request the Example class and call the method "createValue".
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@Documented
@Inherited
public @interface Provider {
}
