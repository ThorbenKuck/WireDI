package com.wiredi.compiler.tests.junit;

import org.junit.jupiter.api.extension.ExtendWith;

import javax.annotation.processing.Processor;
import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(CompilerTestExtension.class)
public @interface CompilerSetup {

    String[] classes() default {};

    String[] folders() default {};

    Class<? extends Processor>[] processors() default {};

    String[] options() default {};

    String rootFolder() default "";
}
