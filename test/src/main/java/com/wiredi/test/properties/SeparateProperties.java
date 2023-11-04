package com.wiredi.test.properties;

import com.wiredi.annotations.properties.PropertyBinding;

@PropertyBinding(file = "separate.properties")
public class SeparateProperties {

    private final String foo;
    private final double pi;

//    public ExampleProperties(String foo, double pi) {
//        this.foo = foo;
//        this.pi = pi;
//    }

    public SeparateProperties(String foo) {
        this.foo = foo;
        this.pi = 0;
    }

    public String getFoo() {
        return foo;
    }

    public double getPi() {
        return pi;
    }

    @Override
    public String toString() {
        return "SeparateProperties{" +
                "foo='" + foo + '\'' +
                ", pi=" + pi +
                '}';
    }
}
