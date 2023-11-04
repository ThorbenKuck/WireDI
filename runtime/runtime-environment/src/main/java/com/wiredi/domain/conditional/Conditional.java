package com.wiredi.domain.conditional;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Inherited
public @interface Conditional {

    Class<? extends ConditionEvaluator> value();

}
