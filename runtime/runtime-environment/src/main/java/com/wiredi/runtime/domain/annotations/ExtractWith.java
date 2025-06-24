package com.wiredi.runtime.domain.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Documented
@Inherited
@Target(ElementType.ANNOTATION_TYPE)
public @interface ExtractWith {
    Class<? extends AnnotationMetadataExtractor> value();
}
