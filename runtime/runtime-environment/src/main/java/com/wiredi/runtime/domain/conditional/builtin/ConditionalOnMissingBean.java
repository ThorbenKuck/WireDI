package com.wiredi.runtime.domain.conditional.builtin;

import com.wiredi.annotations.stereotypes.AliasFor;
import com.wiredi.runtime.domain.conditional.Conditional;
import com.wiredi.runtime.domain.annotations.ExtractWith;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Inherited
@Conditional(ConditionalOnMissingBeanEvaluator.class)
@ExtractWith(ConditionalOnMissingBeanMetadataExtractor.class)
public @interface ConditionalOnMissingBean {

    @AliasFor("type")
    Class<?> value() default Void.class;

    Class<?> type() default Void.class;

}
