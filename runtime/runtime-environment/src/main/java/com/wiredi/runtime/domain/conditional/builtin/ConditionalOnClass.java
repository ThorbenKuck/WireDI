package com.wiredi.runtime.domain.conditional.builtin;

import com.wiredi.runtime.domain.conditional.Conditional;

import java.lang.annotation.*;

/**
 * Conditional annotation that checks if a specified class is present in the classpath.
 * <p>
 * The condition matches when the specified class is available in the classpath.
 * This is useful for conditionally loading components based on the presence of
 * optional dependencies.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * @ConditionalOnClass(className = "com.example.OptionalDependency")
 * public class MyOptionalComponent {
 *     // This component will only be loaded if OptionalDependency is available
 * }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Inherited
@Conditional(ConditionalOnClassEvaluator.class)
public @interface ConditionalOnClass {

    /**
     * The fully qualified name of the class that must be present for the condition to match.
     * @return the class name to check for
     */
    String className();
}
