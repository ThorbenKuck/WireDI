package com.wiredi.tests;

import com.wiredi.tests.instance.Prop;
import com.wiredi.tests.instance.TestProperties;

import java.lang.annotation.Annotation;

public record TestPropertiesInstance(
        String[] files,
        Prop[] properties
) implements TestProperties {

    public static TestPropertiesInstance of(TestProperties annotation) {
        return new TestPropertiesInstance(annotation.files(), annotation.properties());
    }

    private static final TestPropertiesInstance EMPTY = new TestPropertiesInstance(new String[0], new Prop[0]);

    public static TestPropertiesInstance empty() {
        return EMPTY;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return TestProperties.class;
    }
}
