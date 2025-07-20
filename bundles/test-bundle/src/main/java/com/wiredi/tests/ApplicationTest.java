package com.wiredi.tests;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(ApplicationTestExtension.class)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface ApplicationTest {
}
