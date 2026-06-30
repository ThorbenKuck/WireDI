package com.wiredi.annotations.stereotypes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A marker for how the AnnotationMetadata interprets annotations.
 * <p>
 * This annotation is used if the AnnotationMetadata should return the annotated methods value for another name.
 * If you have a method annotated with this annotation, the AnnotationMetadata will return the value of the method.
 * For example:
 *
 * <pre>{@code
 * public @interface MyAnnotation {
 *     @AliasFor("value")
 *     String value() default "";
 *     String name() default "";
 * }
 * }</pre>
 *
 * If this annotation is used and sets the value:
 *
 * <pre>{@code
 * @MyAnnotation("MyClass")
 * class MyClass {
 *
 * }
 * }</pre>
 *
 * It can be accessed via the AnnotationMetadata like this:
 *
 * <pre>{@code
 * MyClass myClass = ...;
 * MyAnntatoion annotation = myClass.getClass().getAnnotation(MyAnnotation.class);
 * AnnotationMetadata metadata = AnnotationMetadata.of(annotation);
 * String className = metadata.getString("name"); // Will be "MyClass"
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AliasFor {
    String value();

    Class<?> nullType() default Void.class;

}
