package com.wiredi.annotations.aspects;

import java.lang.annotation.*;

/**
 * This annotation is used, to mark other annotations as annotations, that will be used for aspects.
 * <p>
 * This annotation is required, if the property "flags.strict-aop-annotation-target" in the
 * "wire-di.processor.properties" is set to true, which is the default behaviour. In this scenario, the annotation
 * processor checks whether annotations in the {@link Aspect#around()} field ar annotated with this annotation. If not,
 * an error is raised.
 * <p>
 * If you do not want to use this annotation (for example when using existing annotations), set the property
 * "flags.strict-aop-annotation-target" to false. Then, this annotation is no longer required.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.ANNOTATION_TYPE)
@Inherited
public @interface AspectTarget {

}
