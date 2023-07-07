package com.wiredi.test;

import com.wiredi.annotations.properties.Properties;

@Properties
public class ExampleProperties {

    private final String foo;
    private final double pi;

    public ExampleProperties(String foo, double pi) {
        this.foo = foo;
        this.pi = pi;
    }
}
