package com.github.thorbenkuck.di.test;

import com.github.thorbenkuck.di.annotations.properties.PropertySource;

@PropertySource(
        file = "test.properties"
)
public class ExampleProperties {

    private final String foo;

    public ExampleProperties(String foo) {
        this.foo = foo;
    }
}
