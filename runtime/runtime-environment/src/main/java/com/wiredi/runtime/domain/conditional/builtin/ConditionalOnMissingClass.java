package com.wiredi.runtime.domain.conditional.builtin;

import com.wiredi.runtime.domain.conditional.Conditional;

import java.lang.annotation.*;

/**
 * Conditional annotation that checks if a specified class is absent from the classpath.
 * <p>
 * The condition matches when the specified class is NOT available in the classpath.
 * This is useful for conditionally loading fallback components when an optional 
 * dependency is not present.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * @ConditionalOnMissingClass(className = "com.example.OptionalDependency")
 * public class FallbackComponent {
 *     // This component will only be loaded if OptionalDependency is NOT available
 * }
 * }
 * </pre>
 * <p>
 * This is the inverse of {@link ConditionalOnClass}.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Inherited
@Conditional(ConditionalOnMissingClassEvaluator.class)
public @interface ConditionalOnMissingClass {

    /**
     * The fully qualified name of the class that must be absent for the condition to match.
     * @return the class name to check for absence
     */
    String className();
}
