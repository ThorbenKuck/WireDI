package com.wiredi.annotations.external;

import java.lang.annotation.*;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Documented
@Inherited
public @interface WireConfiguration {
	WireCandidate[] value();
}
