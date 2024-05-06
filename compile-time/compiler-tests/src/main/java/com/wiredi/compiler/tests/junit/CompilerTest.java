package com.wiredi.compiler.tests.junit;

import org.junit.jupiter.api.Test;

import javax.annotation.processing.Processor;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
@Test
public @interface CompilerTest {

    String[] classes() default {};

    String[] classesIn() default {};

    Class<? extends Processor>[] processors() default {};

    String[] options() default {};

}
