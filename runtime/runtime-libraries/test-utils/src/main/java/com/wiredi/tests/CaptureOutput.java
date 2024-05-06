package com.wiredi.tests;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Autoconfigures the {@link CapturedOutput} for the current test.
 * <p>
 * This annotation can be applied to a test class or method.
 * All output to {@link  System#out} and {@link System#err} will be captured for asserting.
 * <p>
 * The inserted extension will open the {@link CapturedOutput} before each test and close it after each.
 *
 * @see OutputCollector
 * @see CapturedOutput
 * @see CaptureOutputExtension
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(CaptureOutputExtension.class)
public @interface CaptureOutput {

    boolean suppressSystemOut() default false;

    boolean suppressSystemErr() default false;

}
